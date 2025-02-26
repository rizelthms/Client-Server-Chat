package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.Hangup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

public class HangupHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public HangupHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Hangup hangup = new ObjectMapper().readValue(json, Hangup.class);
            Printer.printLineColour("Server disconnected you. Reason: " + hangup.reason(), Printer.ConsoleColour.RED);
            chatCli.getClient().closeConnection();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
