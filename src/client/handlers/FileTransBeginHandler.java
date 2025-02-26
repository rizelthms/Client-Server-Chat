package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.UUIDPayload;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class FileTransBeginHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransBeginHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDPayload uuidPayload = chatCli.getMapper().readValue(json, UUIDPayload.class);
            Printer.printLineColour("File Transfer starting...", Printer.ConsoleColour.GREEN);
            // Check if we are the sender
            if (chatCli.getFileTransferSockets().containsKey(uuidPayload.uuid())) {
                chatCli.getFileTransferSockets().get(uuidPayload.uuid()).sendFile();
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
