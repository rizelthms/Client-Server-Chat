package client.handlers;

import client.shared.Printer;

import java.util.function.Consumer;

public class ParseErrorHandler implements Consumer<String> {
    @Override
    public void accept(String json) {
        Printer.printLineColour("You sent a message that the server could not parse", Printer.ConsoleColour.RED);
    }
}
