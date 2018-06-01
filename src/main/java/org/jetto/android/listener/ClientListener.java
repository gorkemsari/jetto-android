package org.jetto.android.listener;

/**
 *
 * @author gorkemsari - jetto.org
 */
public interface ClientListener {
    void onMessage(String message);
    void onStart(String id);
    void onStop(String id);
    void onError(String message);
}