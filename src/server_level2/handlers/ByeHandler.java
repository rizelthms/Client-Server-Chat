package server_level2.handlers;

import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.ByeResp;
import server_level2.protocol.payloads.Left;

import java.util.function.Consumer;

public class ByeHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public ByeHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        chatClientHandler.sendToClient(new ByeResp(StatusResult.OK));
        String username = chatClientHandler.getUser().username();
        chatClientHandler.disconnect();
        chatClientHandler.getServer().sendToAllExcept(username, new Left(username));
    }
}
