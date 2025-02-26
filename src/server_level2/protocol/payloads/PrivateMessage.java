package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record PrivateMessage(String username, String message) implements Payload {
    @Override
    public Command getCommand() {
        return Command.PM;
    }
}
