package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.UUIDPayload;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class FileTransDoneHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransDoneHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDPayload uuidPayload = chatCli.getMapper().readValue(json, UUIDPayload.class);
            Printer.printLineColour("File transfer finished successfully", Printer.ConsoleColour.GREEN);
            chatCli.getFileReceiveSockets().remove(uuidPayload.uuid());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
