package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record Ready(String version) implements Payload {
    @Override
    public Command getCommand() {
        return Command.READY;
    }
}