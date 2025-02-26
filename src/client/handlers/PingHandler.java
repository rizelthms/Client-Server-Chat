package client.handlers;

import client.cli.ChatCli;
import client.protocol.Command;

import java.util.function.Consumer;

public class PingHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public PingHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        chatCli.getClient().sendPackage(Command.PONG, null);
    }
}
