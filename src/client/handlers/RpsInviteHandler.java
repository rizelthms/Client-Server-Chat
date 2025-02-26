package client.handlers;

import client.cli.ChatCli;
import client.cli.CliCommands;
import client.shared.Printer;
import client.protocol.payloads.Username;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class RpsInviteHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public RpsInviteHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Username username = chatCli.getMapper().readValue(json, Username.class);

            Printer.printLineColour("You have been invited to a rock paper scissors game by " + username.username() +
                    "\nType \"" + CliCommands.COMMAND_ACCEPT + " " + username.username() + "\" to accept the invitation or " +
                    "\n\"" + CliCommands.COMMAND_DECLINE + " " + username.username() + "\" to decline the invitation.", Printer.ConsoleColour.GREEN);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
