package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.Ready;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class ReadyHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public ReadyHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Ready ready = chatCli.getMapper().readValue(json, Ready.class);
            Printer.printLineColour("The server is ready. Server version: " + ready.version(), Printer.ConsoleColour.GREEN);
            chatCli.requestUsername();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
