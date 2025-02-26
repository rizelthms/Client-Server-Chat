package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record RpsStart(String username) implements Payload {
    @Override
    public Command getCommand() {
        return Command.RPS_START;
    }
}
