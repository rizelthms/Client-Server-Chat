package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.cli.User;
import client.protocol.payloads.ListUsersResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class ListUsersHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public ListUsersHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            ListUsersResponse listUsersResponse = chatCli.getMapper().readValue(json, ListUsersResponse.class);
            Printer.printLineColour("Currently logged in users:", client.shared.Printer.ConsoleColour.PURPLE);
            for (User u : listUsersResponse.users()) {
                Printer.printLineColour("\t" + u.getUsername(), Printer.ConsoleColour.PURPLE);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
