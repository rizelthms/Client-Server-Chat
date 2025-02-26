package client.handlers;

import client.cli.ChatCli;
import client.cli.OutgoingFileTransfer;
import client.shared.Printer;
import client.protocol.payloads.FileTransferDecline;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class FileTransferDeclineHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransferDeclineHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            FileTransferDecline fileTransferDecline = chatCli.getMapper().readValue(json, FileTransferDecline.class);
            OutgoingFileTransfer outgoingFileTransfer = chatCli.findSentFileTransferRequest(fileTransferDecline.username(), fileTransferDecline.checksum());
            if (outgoingFileTransfer == null) {
                Printer.printLineColour("A file transfer request that was declined could not be found", Printer.ConsoleColour.RED);
                return;
            }
            chatCli.getSentFtr().remove(outgoingFileTransfer);
            Printer.printLineColour(outgoingFileTransfer.receiverUsername() + " declined your request to receive the file \"" + outgoingFileTransfer.filePath() + "\"", Printer.ConsoleColour.RED);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
