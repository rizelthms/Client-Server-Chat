package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.Status;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class EnterRespHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public EnterRespHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Status status = chatCli.getMapper().readValue(json, Status.class);

            switch (status.status()) {
                case OK -> {
                    Printer.printLineColour("Hello " + chatCli.getCurrentUsername() + ", your login was successful. You can now send messages.", Printer.ConsoleColour.GREEN);
                    new Thread(chatCli::writeToServer).start();
                }
                case ERROR -> {
                    Printer.printLineColour(status.getErrorMessage(), Printer.ConsoleColour.RED);
                    chatCli.requestUsername();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
