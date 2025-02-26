package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.server.ServerFileTransferRequest;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class FileTransferDoneHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public FileTransferDoneHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDPayload uuidPayload = chatClientHandler.getMapper().readValue(json, UUIDPayload.class);
            ServerFileTransferRequest serverFileTransferRequest = null;
            for (ServerFileTransferRequest ftr : chatClientHandler.getServer().getOngoingFileTransfers()) {
                if (ftr.getSenderUUID().equals(uuidPayload.uuid())) {
                    serverFileTransferRequest = ftr;
                }
            }

            if (chatClientHandler.getUser() == null) {
                sendError(6000);
                return;
            }

            if (serverFileTransferRequest == null) {
                sendError(11006);
                return;
            }

            chatClientHandler.getServer().getOngoingFileTransfers().remove(serverFileTransferRequest);
            FileTransferDone donePayload = new FileTransferDone(serverFileTransferRequest.getReceiverUUID());
            chatClientHandler.getServer().sendToUser(serverFileTransferRequest.getReceiverUsername(), donePayload);

            chatClientHandler.sendToClient(new FileTransferDoneResponse(StatusResult.OK, null));
        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new FileTransferDoneResponse(StatusResult.ERROR, errorCode));
    }
}