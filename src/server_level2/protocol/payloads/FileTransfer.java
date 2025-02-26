package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record FileTransfer(String username, String filename, long fileSize, String checksum, String uuid) implements Payload {
    @Override
    public Command getCommand() {
        return Command.FILE_TRANS;
    }
}
