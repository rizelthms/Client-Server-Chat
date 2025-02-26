package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.server.ServerFileTransferRequest;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class FileTransferRequestHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;
    private final Pattern sha256Regex = Pattern.compile("[a-fA-F0-9]{64}");

    public FileTransferRequestHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            FileTransferRequest fileTransferRequest = chatClientHandler.getMapper().readValue(json, FileTransferRequest.class);

            if (chatClientHandler.getUser() == null) {
                sendError(6000);
                return;
            }

            if (!chatClientHandler.getServer().isUserLoggedIn(fileTransferRequest.username())) {
                sendError(11000);
                return;
            }

            if (fileTransferRequest.fileSize() <= 0) {
                sendError(11001);
                return;
            }

            if(!sha256Regex.matcher(fileTransferRequest.checksum()).matches()) {
                sendError(11002);
                return;
            }

            if (chatClientHandler.getUser().username().equals(fileTransferRequest.username())) {
                sendError(11003);
                return;
            }

            ServerFileTransferRequest request = new ServerFileTransferRequest(
                    chatClientHandler.getUser().username(),
                    fileTransferRequest.username(),
                    fileTransferRequest.filename(),
                    fileTransferRequest.fileSize(),
                    fileTransferRequest.checksum()
            );

            chatClientHandler.getServer().getUnansweredFtr().put(request.getReceiverUUID(), request);
            FileTransfer fileTransfer = new FileTransfer(request.getSenderUsername(), request.getFileName(), request.getFileSize(), request.getChecksum(), request.getReceiverUUID());
            chatClientHandler.getServer().sendToUser(request.getReceiverUsername(), fileTransfer);
            chatClientHandler.getServer().sendToUser(chatClientHandler.getUser().username(), new FileTransferResponse(StatusResult.OK, null));

        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new FileTransferResponse(StatusResult.ERROR, errorCode));
    }
}