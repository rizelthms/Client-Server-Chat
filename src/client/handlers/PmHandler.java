package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.PrivateMessage;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class PmHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public PmHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            PrivateMessage pm = chatCli.getMapper().readValue(json, PrivateMessage.class);
            Printer.printLineColour("[Private Message from " + pm.username() + "] " + pm.message(), Printer.ConsoleColour.BLUE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
