package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record Joined(String username) implements Payload {
    @Override
    public Command getCommand() {
        return Command.JOINED;
    }
}
