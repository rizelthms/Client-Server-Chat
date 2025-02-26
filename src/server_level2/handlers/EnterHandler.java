package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.User;
import server_level2.protocol.payloads.EnterResponse;
import server_level2.shared.Printer;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EnterHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;
    private final Pattern usernamePattern = Pattern.compile("[a-zA-Z0-9_]{3,14}");

    public EnterHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            if (json.isBlank()) {
                Printer.printLineColour(chatClientHandler.getUser(), "Empty json received", Printer.ConsoleColour.RED);
                chatClientHandler.sendToClient(new EnterResponse(StatusResult.ERROR, 5001));
            } else {
                User user = chatClientHandler.getMapper().readValue(json, User.class);
                if (chatClientHandler.getUser() != null) {
                    Printer.printLineColour(chatClientHandler.getUser(), "User " + chatClientHandler.getUser().username() + " is already logged in, but tried to log in again as " + user.username(), Printer.ConsoleColour.RED);
                    chatClientHandler.sendToClient(new EnterResponse(StatusResult.ERROR, 5002));
                } else if (chatClientHandler.getServer().isUserLoggedIn(user.username())) {
                    Printer.printLineColour(chatClientHandler.getUser(), "Username " + user.username() + " is already taken", Printer.ConsoleColour.RED);
                    chatClientHandler.sendToClient(new EnterResponse(StatusResult.ERROR, 5000));
                } else if (!usernamePattern.matcher(user.username()).matches()) {
                    Printer.printLineColour(chatClientHandler.getUser(), "Username " + user.username() + " is not a valid username", Printer.ConsoleColour.RED);
                    chatClientHandler.sendToClient(new EnterResponse(StatusResult.ERROR, 5001));
                } else {
                    chatClientHandler.logInUser(user);
                    Printer.printLineColour(chatClientHandler.getUser(), "User " + user.username() + " logged in", Printer.ConsoleColour.GREEN);
                    chatClientHandler.sendToClient(new EnterResponse(StatusResult.OK, null));
                }
            }
        } catch (JsonProcessingException e) {
            Printer.printLineColour(chatClientHandler.getUser(), "Error parsing json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }
}
