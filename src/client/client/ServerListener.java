package client.client;

public interface ServerListener {
    void onServerMessage(String command, String json);
}
