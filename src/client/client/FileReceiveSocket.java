package client.client;

import client.shared.Printer;
import client.shared.Utils;
import client.protocol.payloads.FileTransfer;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class FileReceiveSocket extends Thread{

    private final Socket socket;
    private final FileTransfer fileTransfer;

    public FileReceiveSocket(String ip, FileTransfer fileTransfer) {
        this.fileTransfer = fileTransfer;
        try {
            socket = new Socket(ip, Utils.FILE_TRANSFER_PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            UUID uuid = UUID.fromString(fileTransfer.uuid());

            // send UUID to server for identification
            socket.getOutputStream().write(Utils.uuidToBytes(uuid));

            // receive file
            final Path filePath = Path.of(System.getProperty("user.home") + "/Downloads/" + fileTransfer.filename());
            Files.copy(socket.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Calculating the checksum can take a long time for large files, so we do it in an extra thread
            new Thread(() -> checkChecksums(filePath)).start();
        } catch (IOException e) {
            Printer.printLineColour("Error receiving file", Printer.ConsoleColour.RED);
        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkChecksums(Path filePath) {
        try {
            Printer.printLineColour("Checking checksum...", Printer.ConsoleColour.YELLOW);
            String localChecksum = Utils.getFileHash(filePath.toFile());
            if (fileTransfer.checksum().equals(localChecksum)) {
                Printer.printLineColour("Checksums match, the file \"" + fileTransfer.filename() + "\" was transferred successfully", Printer.ConsoleColour.GREEN);
            } else {
                Printer.printLineColour("Checksums do not match, the file \"" + fileTransfer.filename() + "\" was not transferred successfully", Printer.ConsoleColour.RED);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            Printer.printLineColour("Error calculating file hash", Printer.ConsoleColour.RED);
        }
    }
}
