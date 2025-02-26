package server_level2.server;

import java.util.UUID;

public class ServerFileTransferRequest {

    private final String senderUsername;
    private final String receiverUsername;
    private final String fileName;
    private final long fileSize;
    private final String checksum;
    private final String senderUUID;
    private final String receiverUUID;
    private FileTransferHandler senderHandler;
    private FileTransferHandler receiverHandler;

    public ServerFileTransferRequest(String senderUsername, String receiverUsername, String fileName, long fileSize, String checksum) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.checksum = checksum;
        senderUUID = UUID.randomUUID().toString();
        receiverUUID = UUID.randomUUID().toString();
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getSenderUUID() {
        return senderUUID;
    }

    public String getReceiverUUID() {
        return receiverUUID;
    }

    public FileTransferHandler getSenderHandler() {
        return senderHandler;
    }

    public void setSenderHandler(FileTransferHandler senderHandler) {
        this.senderHandler = senderHandler;
    }

    public FileTransferHandler getReceiverHandler() {
        return receiverHandler;
    }

    public void setReceiverHandler(FileTransferHandler receiverHandler) {
        this.receiverHandler = receiverHandler;
    }
}
