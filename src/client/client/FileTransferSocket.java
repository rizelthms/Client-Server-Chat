package client.client;

import client.cli.ChatCli;
import client.cli.OutgoingFileTransfer;
import client.shared.Printer;
import client.shared.Utils;
import client.protocol.Command;
import client.protocol.payloads.UUIDPayload;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

public class FileTransferSocket {

    private final ChatCli chatCli;
    private final Socket socket;
    private final OutgoingFileTransfer outgoingFileTransfer;

    public FileTransferSocket(ChatCli chatCli, OutgoingFileTransfer outgoingFileTransfer) {
        this.chatCli = chatCli;
        this.outgoingFileTransfer = outgoingFileTransfer;
        try {
            socket = new Socket(chatCli.getClient().getIp(), Utils.FILE_TRANSFER_PORT);
            socket.getOutputStream().write(Utils.uuidToBytes(outgoingFileTransfer.uuid()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFile() {
        new Thread(() -> {
            try {
                Files.copy(outgoingFileTransfer.filePath(), socket.getOutputStream());
                chatCli.getClient().sendPackage(Command.FILE_TRANS_DONE, chatCli.getMapper().writeValueAsString(new UUIDPayload(outgoingFileTransfer.uuid().toString())));
                socket.close();
                chatCli.getFileTransferSockets().remove(outgoingFileTransfer.uuid().toString());
            } catch (IOException e) {
                Printer.printLineColour("The file could not be sent, closing socket...", Printer.ConsoleColour.RED);
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }).start();
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
