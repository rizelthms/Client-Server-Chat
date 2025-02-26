package server_level2.protocol.payloads;

import server_level2.protocol.Command;

public record PongError(int code) implements Payload{
    @Override
    public Command getCommand() {
        return Command.PONG_ERROR;
    }
}
