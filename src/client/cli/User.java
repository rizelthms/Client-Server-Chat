package client.cli;

import client.shared.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    private static int colourIndex = 0;

    private final String username;

    public Printer.ConsoleColour getColour() {
        return colour;
    }

    public String getUsername() {
        return username;
    }

    private final Printer.ConsoleColour colour;

    public User(@JsonProperty("username") String username) {
        this.username = username;
        colour = Printer.ConsoleColour.values()[colourIndex % Printer.ConsoleColour.values().length];
        colourIndex++;
    }
}
