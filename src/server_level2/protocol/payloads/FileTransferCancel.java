package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record FileTransferCancel(String uuid) implements Payload {
    @Override
    public Command getCommand() {
        return Command.FILE_TRANS_CANCEL;
    }
}
