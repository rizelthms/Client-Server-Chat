package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.UUIDStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class FileTransferDeclineResponseHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransferDeclineResponseHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDStatus status = chatCli.getMapper().readValue(json, UUIDStatus.class);
            switch (status.status()) {
                case OK -> {
                    Printer.printLineColour("File transfer declined successfully.", Printer.ConsoleColour.GREEN);
                    chatCli.getReceivedFtr().remove(status.uuid());
                }
                case ERROR -> Printer.printLineColour(  "File transfer could not be declined: " + status.getErrorMessage(), Printer.ConsoleColour.RED);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
