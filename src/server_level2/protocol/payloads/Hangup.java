package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record Hangup(int reason) implements Payload {
    @Override
    public Command getCommand() {
        return Command.HANGUP;
    }
}
