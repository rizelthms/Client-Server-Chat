package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;

import java.util.function.Consumer;

public class ByeResponseHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public ByeResponseHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        Printer.printLineColour("Disconnected by server.", Printer.ConsoleColour.RED);
        chatCli.getClient().closeConnection();
    }
}
