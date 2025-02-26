package client.handlers;

import client.shared.Printer;

import java.util.function.Consumer;

public class RpsTimeoutHandler implements Consumer<String> {

    @Override
    public void accept(String json) {
        Printer.printLineColour("One of the players took too long to make a choice. The game ended.", Printer.ConsoleColour.RED);
    }
}
