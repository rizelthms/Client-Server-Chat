package client.handlers;

import client.shared.Printer;
import client.protocol.payloads.Username;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

public class JoinedHandler implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Username username = new ObjectMapper().readValue(json, Username.class);
            Printer.printLineColour(username.username() + " has joined the chat.", Printer.ConsoleColour.GREEN);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
