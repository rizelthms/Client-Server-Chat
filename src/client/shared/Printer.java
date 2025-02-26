package client.shared;

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
        System.out.println(COLORS[colour.ordinal()] + message + RESET);
    }

    public static void printColourBold(String message, ConsoleColour colour) {
        System.out.printf("\033[1m" + COLORS[colour.ordinal()] + message + RESET);
    }
}
