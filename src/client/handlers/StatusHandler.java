package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.Status;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class StatusHandler implements Consumer<String> {

    private final ChatCli chatCli;
    private final String positiveMessage;
    private final String negativeMessage;

    public StatusHandler(ChatCli chatCli, String positiveMessage, String negativeMessage) {
        this.chatCli = chatCli;
        this.positiveMessage = positiveMessage;
        this.negativeMessage = negativeMessage;
    }

    @Override
    public void accept(String json) {
        try {
            Status status = chatCli.getMapper().readValue(json, Status.class);
            switch (status.status()) {
                case OK -> Printer.printLineColour(positiveMessage, Printer.ConsoleColour.GREEN);
                case ERROR -> Printer.printLineColour(negativeMessage + ": " + status.getErrorMessage(), Printer.ConsoleColour.RED);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
