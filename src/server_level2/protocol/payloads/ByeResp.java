package server_level2.protocol.payloads;

import server_level2.protocol.Command;
import server_level2.protocol.StatusResult;

public record ByeResp(StatusResult status) implements Payload {
    @Override
    public Command getCommand() {
        return Command.BYE_RESP;
    }
}
