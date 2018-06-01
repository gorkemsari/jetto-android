jetto is open source library to develop chat apps, real-time games, social media apps etc. for mobile devices. 

jetto provides real-time bi-directional & secure communication. It creates TCP/IP based encrypted communication between server and clients.

Visit [jetto official website](http://www.jetto.org)

# Installation
Android **jetto** library can be added to project from gradle and maven by adding below codes.

## Gradle
```java
compile 'org.jetto:jetto-android:1.0.0'
```

## Maven
```java
<dependency>
    <groupId>org.jetto</groupId>
    <artifactId>jetto-android</artifactId>
    <version>1.0.0</version>
    <type>pom</type>
</dependency>
```

# Start Client
- Create an activity class on your Android project

```java
/**
*
* @author gorkemsari
*/
public class MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```

- Add **ClientListener** interface to class
- Abstract methods should be added **onMessage, onStart, onStop, onError** automatically depends on your IDE
- If not added, abstract methods should be defined with **@Override** annotation manually

```java
import org.jetto.listener.ClientListener;
    
/**
*
* @author gorkemsari
*/
public class MainActivity implements ClientListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onMessage(String message, String id) {
        
    }

    @Override
    public void onStart(String id) {
        
    }

    @Override
    public void onStop(String id) {
        
    }

    @Override
    public void onError(String message, String id) {
        
    }
}
```

- Add new **ClientEndpoint** with defined server address and port number 
- Set listener to **ClientEndpoint** and start client
- Now client sends connection request to server
- If connection request is received by server successfully, a random id is sent from server to client and new encryption key produced by **Diffie-Hellman** key exchange algorithm and secure communication is started between server and client. **Aes** encryption algorithm is used to encryption.

```java
import org.jetto.listener.ClientListener;
import org.jetto.endpoint.ClientEndpoint;

/**
*
* @author gorkemsari
*/
public class MainActivity implements ClientListener {
    
    ClientEndpoint client;
    String serverIpAddress = "192.168.1.24";
    int serverPort = 2329;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new ClientEndpoint(serverIpAddress, serverPort);
        client.setClientListener(this);
        client.start();
    }

    @Override
    public void onMessage(String message, String id) {
        
    }

    @Override
    public void onStart(String id) {
        
    }

    @Override
    public void onStop(String id) {
        
    }

    @Override
    public void onError(String message, String id) {
        
    }
}
```

# Create Model
Models should be extended by **Model** class of **jetto**. **Model** class has **type** property for parsing processes.

```java
import org.jetto.model.Model;

/**
*
* @author gorkemsari
*/
public class MessageModel extends Model {
    private String header;
    private String message;

    /**
    * @return the header
    */
    public String getHeader() {
        return header;
    }

    /**
    * @param header the header to set
    */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
    * @return the message
    */
    public String getMessage() {
        return message;
    }

    /**
    * @param message the message to set
    */
    public void setMessage(String message) {
        this.message = message;
    }
}
```

# Parse to Json
As mentioned above, models are extended by **Model** class of **jetto**. While setting models properties also **type** property of **Model** should be set. Models are parsed to json by **toJson()** method of **Parser** class. **Parser** uses **gson** library on background for parsing process.

```java
import org.jetto.parser.Parser;

/**
*
* @author gorkemsari
*/
MessageModel messageModel = new MessageModel();
messageModel.setHeader("This is header");
messageModel.setMessage("Hello from Android!");
messageModel.setType(1);//setType is extended from Model class of jetto

Parser parser = new Parser();//parser class of jetto
String json = parser.toJson(messageModel);
```

# Parse to Model
The message type can be retrieved by **getType()** method of **Parser** when a json message is received. Json messages are parsed to related model by **toModel()** method of **Parser** class according to type.

```java
import org.jetto.parser.Parser;

/**
*
* @author gorkemsari
*/
String json = "{
        "header": "This is header",
        "message": "Hello from Android!",
        "type": "1"
    }";
Parser parser = new Parser();//parser class of jetto
int type = parser.getType(json);

switch (type) {
    case 1:  
        MessageModel messageModel = parser.toModel(json, MessageModel.class);
        ...
        break;
    case 2:  
        OtherModel otherModel = parser.toModel(json, OtherModel.class);
        ...
        break;
    default: 
        ...
        break;
}
```

# Send Message
**jetto** sends all messages as byte array on background. Any format of string or json can be selected on high level usage. All messages will be caught on the **onMessage** method of server.

## Send String Message to Server
```java
client.write("Hello from Android!");
```

## Send String Message to Specific Client
This message will be redirected to specific client by server on background. id is produced by server at begining of connection process and sent to client to define it as unique.

```java
String id = "5345ud-9ur6j-dfg34-3e82gb";//specific client id
client.write("Hello from Android!", id);
```

## Send String Message to Specific Clients
This message will be redirected to specific clients by server on background. Define id list of specific clients.

```java
List<String> idList = new ArrayList<>();
idList.add("5345ud-9ur6j-dfg34-3e82gb");//client id
idList.add("e4t56-l9k2nb-0iplm-441e7y");//client id
idList.add("8yfd5s-33vbn6-27y5r-r5fxm");//client id

client.write("Hello from Android!", idList);
```

## Send Json Message to Server
```java
MessageModel messageModel = new MessageModel();
messageModel.setHeader("This is header");
messageModel.setMessage("Hello from Android!");
messageModel.setType(1);//setType is extended from Model class or jetto

Parser parser = new Parser();//parser class of jetto
client.write(parser.toJson(messageModel));
```

## Send Json Message to Specific Client
This message will be redirected to specific client by server on background.

```java
String id = "5345ud-9ur6j-dfg34-3e82gb";//client id

MessageModel messageModel = new MessageModel();
messageModel.setHeader("This is header");
messageModel.setMessage("Hello from Android!");
messageModel.setType(1);//setType is extended from Model class or jetto

Parser parser = new Parser();//parser class of jetto
client.write(parser.toJson(messageModel), id);
```

## Send Json Message to Specific Clients
This message will be redirected to specific clients by server on background.

```java
List<String> idList = new ArrayList<>();
idList.add("5345ud-9ur6j-dfg34-3e82gb");//client id
idList.add("e4t56-l9k2nb-0iplm-441e7y");//client id
idList.add("8yfd5s-33vbn6-27y5r-r5fxm");//client id

MessageModel messageModel = new MessageModel();
messageModel.setHeader("This is header");
messageModel.setMessage("Hello from Android!");
messageModel.setType(1);//setType is extended from Model class or jetto

Parser parser = new Parser();//parser class of jetto
client.write(parser.toJson(messageModel), idList);
```

# Receive Message
**jetto** receives all messages as byte array on background. All messages will be caught on the **onMessage** method of client.

```java
import org.jetto.parser.Parser;

/**
*
* @author gorkemsari
*/
@Override
public void onMessage(String message) {

    Parser parser = new Parser();//parser class of jetto
    int type = parser.getType(message);

    switch (type) {
        case 1:  
            MessageModel messageModel = parser.toModel(message, MessageModel.class);
            ...
            break;
        case 2:  
            OtherModel otherModel = parser.toModel(message, OtherModel.class);
            ...
            break;
        default: 
            ...
            break;
    }
}
```
