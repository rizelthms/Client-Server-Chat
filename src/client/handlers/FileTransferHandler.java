package client.handlers;

import client.cli.ChatCli;
import client.cli.CliCommands;
import client.shared.Printer;
import client.protocol.payloads.FileTransfer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class FileTransferHandler implements Consumer<String> {

    private static final long BYTE = 1L;
    private static final long KiB = BYTE * 1024;
    private static final long MiB = KiB * 1024;
    private static final long GiB = MiB * 1024;
    private static final long KB = BYTE * 1000;
    private static final long MB = KB * 1000;
    private static final long GB = MB * 1000;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private final ChatCli chatCli;

    public FileTransferHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            FileTransfer fileTransfer = chatCli.getMapper().readValue(json, FileTransfer.class);
            Printer.printLineColour("You received a new file transfer request from " + fileTransfer.username(), Printer.ConsoleColour.PURPLE);
            Printer.printLineColour("\tFilename: " + fileTransfer.filename(), Printer.ConsoleColour.PURPLE);
            Printer.printLineColour("\tFile size: " + getHumanReadableSize(fileTransfer.fileSize()), Printer.ConsoleColour.PURPLE);
            Printer.printLineColour("Type \"" + CliCommands.COMMAND_ACCEPT_FILE + " " + fileTransfer.uuid() + "\" to accept the file and", Printer.ConsoleColour.PURPLE);
            Printer.printLineColour("\"" + CliCommands.COMMAND_DECLINE_FILE + " " + fileTransfer.uuid() + "\" to decline the file transfer request", Printer.ConsoleColour.PURPLE);

            chatCli.getReceivedFtr().put(fileTransfer.uuid(), fileTransfer);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHumanReadableSize(long size) {
        String binaryString;
        if (size >= GiB) binaryString = formatSize(size, GiB, "GiB");
        else if (size >= MiB) binaryString = formatSize(size, MiB, "MiB");
        else if (size >= KiB) binaryString = formatSize(size, KiB, "KiB");
        else binaryString = formatSize(size, BYTE, "Bytes");

        String decimalString;
        if (size >= GB) decimalString = formatSize(size, GB, "GB");
        else if (size >= MB) decimalString = formatSize(size, MB, "MB");
        else if (size >= KB) decimalString = formatSize(size, KB, "KB");
        else decimalString = formatSize(size, BYTE, "Bytes");

        return decimalString + " (" + binaryString + ")";
    }

    private String formatSize(long size, long divider, String unitName) {
        return decimalFormat.format((double) size / divider) + " " + unitName;
    }
}