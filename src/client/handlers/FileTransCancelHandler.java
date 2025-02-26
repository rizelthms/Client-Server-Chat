package client.handlers;

import client.cli.ChatCli;
import client.client.FileReceiveSocket;
import client.client.FileTransferSocket;
import client.shared.Printer;
import client.protocol.payloads.UUIDPayload;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class FileTransCancelHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransCancelHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDPayload uuidPayload = chatCli.getMapper().readValue(json, UUIDPayload.class);
            Printer.printLineColour("File transfer has been canceled", Printer.ConsoleColour.RED);
            FileReceiveSocket fileReceiveSocket = chatCli.getFileReceiveSockets().get(uuidPayload.uuid());
            if (fileReceiveSocket != null) {
                fileReceiveSocket.closeSocket();
                chatCli.getFileReceiveSockets().remove(uuidPayload.uuid());
            }
            FileTransferSocket fileTransferSocket = chatCli.getFileTransferSockets().get(uuidPayload.uuid());
            if (fileTransferSocket != null) {
                fileTransferSocket.closeSocket();
                chatCli.getFileTransferSockets().remove(uuidPayload.uuid());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
