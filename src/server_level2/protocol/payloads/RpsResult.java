package server_level2.protocol.payloads;

import server_level2.protocol.Choice;
import server_level2.protocol.Command;
import server_level2.protocol.Result;

public record RpsResult(Result result, Choice otherChoice) implements Payload{
    @Override
    public Command getCommand() {
        return Command.RPS_RESULT;
    }
}
