package server_level2.server;

import server_level2.protocol.payloads.FileTransferBegin;
import server_level2.shared.Printer;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class FileTransferHandler extends Thread {

    private final Socket socket;
    private final Server server;
    private DisconnectionListener disconnectionListener;
    private UUID uuid;
    private boolean isSenderSocket;
    private ServerFileTransferRequest fileTransferRequest;

    public FileTransferHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void setDisconnectionListener(DisconnectionListener disconnectionListener) {
        this.disconnectionListener = disconnectionListener;
    }

    public void disconnect() {
        if (!socket.isClosed()) {
            try {
                socket.getOutputStream().flush();
                Printer.printLineColour(uuid,"Disconnecting...", Printer.ConsoleColour.RED);
                if(disconnectionListener != null) {
                    disconnectionListener.onDisconnect();
                }
                socket.getInputStream().close();
                socket.close();
                interrupt();
            } catch (IOException e) {
                Printer.printLineColour(uuid, "Error disconnecting...", Printer.ConsoleColour.RED);
            }
        }
    }

    public void sendBytes(byte[] buffer, int len) {
        try {
            socket.getOutputStream().write(buffer, 0, len);
        } catch (IOException e) {
            Printer.printLineColour(uuid, "Receiver disconnected", Printer.ConsoleColour.RED);
            for (ServerFileTransferRequest ftr : server.getOngoingFileTransfers()) {
                if (ftr.getReceiverUUID().equals(uuid.toString())) {
                    server.cancelFileTransfer(ftr);
                }
            }
        }
    }

    public String getUUID() {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }

    @Override
    public void run() {
        try {
            byte[] uuidBytes = socket.getInputStream().readNBytes(16);
            ByteBuffer uuidByteBuffer = ByteBuffer.wrap(uuidBytes);
            long high = uuidByteBuffer.getLong();
            long low = uuidByteBuffer.getLong();
            uuid = new UUID(high, low);

            boolean found = false;
            for (ServerFileTransferRequest ftr : server.getOngoingFileTransfers()) {
                if (ftr.getSenderUUID().equals(uuid.toString())) {
                    isSenderSocket = true;
                    fileTransferRequest = ftr;
                    fileTransferRequest.setSenderHandler(this);
                    found = true;
                    break;
                } else if (ftr.getReceiverUUID().equals(uuid.toString())) {
                    isSenderSocket = false;
                    fileTransferRequest = ftr;
                    fileTransferRequest.setReceiverHandler(this);
                    found = true;
                }
            }
            if (!found) {
                Printer.printLineColour(uuid, "No file request with UUID " + uuid + " was found. Disconnecting...", Printer.ConsoleColour.RED);
                disconnect();
                return;
            }

            // if both sockets connected let the file transfer begin
            if (fileTransferRequest.getSenderHandler() != null && fileTransferRequest.getReceiverHandler() != null) {
                server.sendToUser(fileTransferRequest.getSenderUsername(), new FileTransferBegin(fileTransferRequest.getSenderUUID()));
                server.sendToUser(fileTransferRequest.getReceiverUsername(), new FileTransferBegin(fileTransferRequest.getReceiverUUID()));
            }

            if (isSenderSocket) {
                byte[] buf = new byte[4096];
                while (!socket.isInputShutdown()) {
                    int n = socket.getInputStream().read(buf);
                    if( n < 0 ) {
                        break;
                    }
                    // send to receiver
                    fileTransferRequest.getReceiverHandler().sendBytes(buf, n);
                }
                Printer.printLineColour(uuid, "Disconnecting receiver and then sender...", Printer.ConsoleColour.BLUE);
                fileTransferRequest.getReceiverHandler().disconnect();
                disconnect();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
