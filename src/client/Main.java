package client;

import client.cli.ChatCli;

public class Main {
    public static void main(String[] args) {
        new ChatCli("127.0.0.1", 1337);
    }
}
