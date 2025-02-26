package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record FileTransferAccept(String username, String checksum, String uuid) implements Payload {
    @Override
    public Command getCommand() {
        return Command.FILE_TRANS_ACCEPT;
    }
}
