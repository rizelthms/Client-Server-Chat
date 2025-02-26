package client.handlers;

import client.cli.ChatCli;
import client.shared.Printer;
import client.protocol.payloads.RpsResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public class RpsResultHandler implements Consumer<String> {

    private final ChatCli chatCli;

    public RpsResultHandler(ChatCli chatCli) {
        this.chatCli = chatCli;
    }

    @Override
    public void accept(String json) {
        try {
            RpsResult result = chatCli.getMapper().readValue(json, RpsResult.class);

            switch (result.result()) {
                case WON -> Printer.printLineColour("Congratulations, you won! The other player picked " + result.otherChoice(), Printer.ConsoleColour.GREEN);
                case LOST -> Printer.printLineColour("You lost. The other player picked " + result.otherChoice(), Printer.ConsoleColour.RED);
                case TIE -> Printer.printLineColour("Tied game. The other player also picked " + result.otherChoice(), Printer.ConsoleColour.YELLOW);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
