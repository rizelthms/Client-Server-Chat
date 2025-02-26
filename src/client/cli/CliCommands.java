package client.cli;

import java.util.regex.Pattern;

public class CliCommands {
    public static final String COMMAND_EXIT = "exit";
    public static final String COMMAND_HELP = "help";
    public static final String COMMAND_LIST_USERS = "list_users";
    public static final String COMMAND_PM = "pm";
    public static final String COMMAND_RPS = "rps";
    public static final String COMMAND_ACCEPT = "accept";
    public static final String COMMAND_DECLINE = "decline";
    public static final String COMMAND_CHOICE = "choice";
    public static final String COMMAND_FILE_TRANSFER = "transfer_file";
    public static final String COMMAND_ACCEPT_FILE = "accept_file";
    public static final String COMMAND_DECLINE_FILE = "decline_file";

    public static final String COMMAND_ROCK = "rock";
    public static final String COMMAND_PAPER = "paper";
    public static final String COMMAND_SCISSORS = "scissors";

    public static final Pattern PATTERN_PM = Pattern.compile(COMMAND_PM + " [^ ]+ \".+\"");
    public static final Pattern PATTERN_PM_MESSAGE = Pattern.compile("\".+\"");
    public static final Pattern PATTERN_RPS = Pattern.compile(COMMAND_RPS + " [^ ]+");
    public static final Pattern PATTERN_ACCEPT = Pattern.compile(COMMAND_ACCEPT + " [^ ]+");
    public static final Pattern PATTERN_DECLINE = Pattern.compile(COMMAND_DECLINE + " [^ ]+");
    public static final Pattern PATTERN_CHOICE = Pattern.compile(COMMAND_CHOICE + " (" + COMMAND_ROCK + "|" + COMMAND_PAPER + "|" + COMMAND_SCISSORS + ")");
    public static final Pattern PATTERN_FILE_TRANSFER = Pattern.compile(COMMAND_FILE_TRANSFER + " [^ ]+ \".+\"");
    public static final Pattern PATTERN_ACCEPT_FILE = Pattern.compile(COMMAND_ACCEPT_FILE + " .+");
    public static final Pattern PATTERN_DECLINE_FILE = Pattern.compile(COMMAND_DECLINE_FILE + " .+");
}
