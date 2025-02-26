package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.Username;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class RpsDeclineHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public RpsDeclineHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Username username = chatCli.getMapper().readValue(json, Username.class);

            Printer.printLineColour(username.username() + " declined your invitation to a rock, paper, scissors game", Printer.ConsoleColour.RED);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}