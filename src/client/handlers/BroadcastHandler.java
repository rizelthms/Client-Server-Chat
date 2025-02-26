package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.cli.User;
import client.protocol.payloads.Broadcast;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class BroadcastHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public BroadcastHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            Broadcast broadcast = chatCli.getMapper().readValue(json, Broadcast.class);

            User user = chatCli.getUsers().stream()
                    .filter(u -> u.getUsername().equals(broadcast.username()))
                    .findFirst()
                    .orElseGet(() -> {
                        User newUser =  new User(broadcast.username());
                        chatCli.getUsers().add(newUser);
                        return newUser;
                    });
            Printer.printLineColour("[" + user.getUsername() + "]: " + broadcast.message(), user.getColour());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
