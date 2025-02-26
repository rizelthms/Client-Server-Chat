package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record FileTransferDecline(String username, String checksum) implements Payload {
    @Override
    public Command getCommand() {
        return Command.FILE_TRANS_DECLINE;
    }
}
