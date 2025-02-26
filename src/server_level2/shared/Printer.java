package server_level2.shared;

import server_level2.protocol.payloads.User;

import java.util.UUID;

public class Printer {
    public enum ConsoleColour {
        BLUE, PURPLE, CYAN, YELLOW, GREEN, RED, WHITE, BLACK
    }

    private final static String RESET = "\033[0m";
    private final static String[] COLORS = {
            "\033[0;34m", "\033[0;35m", "\033[0;36m", "\033[0;33m",
            "\033[0;32m", "\033[0;31m", "\033[0;37m", "\033[0;30m"
    };

    public static void printLineColour(String message, ConsoleColour colour) {
        printLineColour((User) null, message, colour);
    }

    public static void printLineColour(User user, String message, ConsoleColour colour) {
        String prefix = "";
        if (user != null) {
            prefix = "[User: " + user.username() + "]\t";
        }
        System.out.println(COLORS[colour.ordinal()] + prefix + message + RESET);
    }

    public static void printLineColour(UUID uuid, String message, ConsoleColour colour) {
        String prefix = "";
        if (uuid != null) {
            prefix = "[UUID: " + uuid + "]\t";
        }
        System.out.println(COLORS[colour.ordinal()] + prefix + message + RESET);
    }
}
