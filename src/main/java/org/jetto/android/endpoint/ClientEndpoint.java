package org.jetto.android.endpoint;


import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import org.jetto.android.common.Common;
import org.jetto.android.common.Enums;
import org.jetto.android.crypto.Aes;
import org.jetto.android.crypto.DiffieHellman;
import org.jetto.android.listener.ClientListener;
import org.jetto.android.listener.ThreadListener;
import org.jetto.android.model.MessageModel;
import org.jetto.android.model.RegisterModel;
import org.jetto.android.parser.Parser;
import org.jetto.android.worker.Reader;
import org.jetto.android.worker.Writer;

/**
 *
 * @author gorkemsari - jetto.org
 */
public class ClientEndpoint implements ThreadListener {
    private final String ip;
    private final int port;
    private Writer writer;
    private Reader reader;
    private ClientListener clientListener;
    private final Parser parser;
    private final Aes aes;
    private String aesKey;

    public ClientEndpoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.aes = new Aes();
        this.parser = new Parser();
        this.aesKey = Common.DEFAULT_KEY;
    }

    public void setClientListener(ClientListener clientListener){
        this.clientListener = clientListener;
    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ip, port);
                    reader = new Reader(socket, ClientEndpoint.this);
                    writer = new Writer(socket, ClientEndpoint.this);
                } catch (IOException e) {
                    clientListener.onError(e.getMessage());
                }
            }
        }).start();
    }

    public void stop(){
        this.clientListener = null;
        reader.close();
        writer.close();
        clientListener.onStop(reader.getId());
    }

    public void write(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageModel model = new MessageModel();
                    model.setType(Enums.Type.SERVER.Value);
                    model.setFrom(reader.getId());
                    model.setMessage(message);

                    byte[] encryptedData = aes.encrypt(parser.toJson(model), aesKey);
                    writer.write(encryptedData);
                } catch (Exception e) {
                    clientListener.onError(e.getMessage());
                }
            }
        }).start();
    }

    public void write(final String message, final String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageModel model = new MessageModel();
                    model.setType(Enums.Type.FORWARD.Value);
                    model.setFrom(reader.getId());
                    model.setTo(Arrays.asList(id));
                    model.setMessage(message);

                    byte[] encryptedData = aes.encrypt(parser.toJson(model), aesKey);
                    writer.write(encryptedData);
                } catch (Exception e) {
                    clientListener.onError(e.getMessage());
                }
            }
        }).start();
    }

    public void write(final String message, final List<String> id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageModel model = new MessageModel();
                    model.setType(Enums.Type.FORWARD.Value);
                    model.setFrom(reader.getId());
                    model.setTo(id);
                    model.setMessage(message);

                    byte[] encryptedData = aes.encrypt(parser.toJson(model), aesKey);
                    writer.write(encryptedData);
                } catch (Exception e) {
                    clientListener.onError(e.getMessage());
                }
            }
        }).start();
    }

    private void register(final byte[] encryptedData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writer.write(encryptedData);
                }  catch (Exception e) {
                    clientListener.onError(e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void onMessage(byte[] message, String id) {
        try {
            MessageModel messageModel = parser.toModel(aes.decrypt(message, aesKey), MessageModel.class);

            int subType = parser.getType(messageModel.getMessage());
            if(subType == Enums.SubType.REGISTER.Value){
                RegisterModel registerModel = parser.toModel(messageModel.getMessage(), RegisterModel.class);
                reader.setId(registerModel.getId());

                DiffieHellman dh = new DiffieHellman();
                int privateKey = dh.getNewPrivateKey();
                int publicKey = dh.getNewPublicKey(privateKey);
                int serverPublicKey = registerModel.getPublicKey();
                int commonKey = dh.getNewCommonKey(serverPublicKey, privateKey);

                RegisterModel regModel = new RegisterModel();
                regModel.setPublicKey(publicKey);
                regModel.setId(registerModel.getId());
                regModel.setType(Enums.SubType.REGISTER.Value);

                MessageModel msgModel = new MessageModel();
                msgModel.setType(Enums.Type.SERVER.Value);
                msgModel.setFrom(registerModel.getId());
                msgModel.setMessage(parser.toJson(regModel));

                register(aes.encrypt(parser.toJson(msgModel), aesKey));

                aesKey = registerModel.getId() + "-" + commonKey;
                clientListener.onStart(registerModel.getId());
            }
            else{
                clientListener.onMessage(messageModel.getMessage());
            }
        }  catch (Exception e) {
            clientListener.onError(e.getMessage());
        }
    }

    @Override
    public void onError(String message, String id) {
        clientListener.onError(message);
        clientListener.onStop(id);
    }
}