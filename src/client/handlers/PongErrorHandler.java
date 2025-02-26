package client.handlers;

import client.shared.Printer;
import client.protocol.payloads.PongError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

public class PongErrorHandler implements Consumer<String> {
    @Override
    public void accept(String json) {
        try {
            PongError pongError = new ObjectMapper().readValue(json, PongError.class);
            Printer.printLineColour("Received unexpected PONG. Error code: " + pongError.code(), Printer.ConsoleColour.RED);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
