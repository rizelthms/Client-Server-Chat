package server_level2.protocol.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;
import server_level2.protocol.Command;
import server_level2.protocol.StatusResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileTransferDoneResponse(StatusResult status, Integer code) implements Payload {
    @Override
    public Command getCommand() {
        return Command.FILE_TRANS_DONE_RESP;
    }
}
