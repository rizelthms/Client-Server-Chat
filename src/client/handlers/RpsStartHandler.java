package client.handlers;

import client.cli.ChatCli;
import client.cli.CliCommands;
import client.shared.Printer;
import client.protocol.payloads.Username;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class RpsStartHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public RpsStartHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Username username = chatCli.getMapper().readValue(json, Username.class);

            Printer.printLineColour("A rock, paper, scissors game has been started with " + username.username() + ".\n" +
                    "Type \"" + CliCommands.COMMAND_CHOICE + " [choice]\" to choose your hand.", Printer.ConsoleColour.RED);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
