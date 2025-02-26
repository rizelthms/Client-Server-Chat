package client.handlers;

import client.cli.ChatCli;
import client.client.FileTransferSocket;
import client.cli.OutgoingFileTransfer;
import client.shared.Printer;
import client.protocol.payloads.FileTransferAccept;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;
import java.util.function.Consumer;

public class FileTransferAcceptHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransferAcceptHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            FileTransferAccept fileTransferAccept = chatCli.getMapper().readValue(json, FileTransferAccept.class);
            OutgoingFileTransfer outgoingFileTransfer = chatCli.findSentFileTransferRequest(fileTransferAccept.username(), fileTransferAccept.checksum());
            if (outgoingFileTransfer == null) {
                Printer.printLineColour("A file transfer request that was accepted could not be found", Printer.ConsoleColour.RED);
                return;
            }
            chatCli.getSentFtr().remove(outgoingFileTransfer);
            Printer.printLineColour(fileTransferAccept.username() + " accepted your file transfer request for the file \"" + outgoingFileTransfer.filePath() + "\"", Printer.ConsoleColour.GREEN);
            Printer.printLineColour("Starting file transfer...", Printer.ConsoleColour.GREEN);
            chatCli.getFileTransferSockets().put(fileTransferAccept.uuid(), new FileTransferSocket(chatCli, new OutgoingFileTransfer(
                    outgoingFileTransfer.receiverUsername(),
                    outgoingFileTransfer.filePath(),
                    outgoingFileTransfer.checksum(),
                    UUID.fromString(fileTransferAccept.uuid())
            )));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
