package server_level2.handlers;

import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.ListUsersResponse;
import server_level2.protocol.payloads.User;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class ListUsersHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public ListUsersHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        if (chatClientHandler.getUser() != null) {
            Printer.printLineColour("Requested user list", Printer.ConsoleColour.PURPLE);
            chatClientHandler.sendToClient(new ListUsersResponse(chatClientHandler.getServer().getUsers().toArray(new User[0]), null, null));
        } else {
            Printer.printLineColour("User that is not logged in tried to list users", Printer.ConsoleColour.RED);
            chatClientHandler.sendToClient(new ListUsersResponse(null, StatusResult.ERROR, 6000));
        }
    }
}
