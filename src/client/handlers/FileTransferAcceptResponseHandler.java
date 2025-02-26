package client.handlers;

import client.cli.ChatCli;
import client.client.FileReceiveSocket;
import client.shared.Printer;
import client.protocol.payloads.UUIDStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class FileTransferAcceptResponseHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public FileTransferAcceptResponseHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDStatus status = chatCli.getMapper().readValue(json, UUIDStatus.class);
            switch (status.status()) {
                case OK -> {
                    Printer.printLineColour("File transfer accepted successfully, initiating file transfer...", Printer.ConsoleColour.GREEN);
                    FileReceiveSocket receiveSocket = new FileReceiveSocket(chatCli.getClient().getIp(), chatCli.getReceivedFtr().get(status.uuid()));
                    chatCli.getFileReceiveSockets().put(status.uuid(), receiveSocket);
                    receiveSocket.start();
                    chatCli.getReceivedFtr().remove(status.uuid());
                }
                case ERROR -> Printer.printLineColour(  "File transfer could not be accepted: " + status.getErrorMessage(), Printer.ConsoleColour.RED);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
