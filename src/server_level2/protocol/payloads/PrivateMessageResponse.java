package server_level2.protocol.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;
import server_level2.protocol.Command;
import server_level2.protocol.StatusResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PrivateMessageResponse(StatusResult status, Integer code) implements Payload {
    @Override
    public Command getCommand() {
        return Command.PM_RESP;
    }
}
