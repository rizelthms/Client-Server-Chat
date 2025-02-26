package client.cli;

import client.client.Client;
import client.client.FileReceiveSocket;
import client.client.FileTransferSocket;
import client.client.ServerListener;
import client.shared.Printer;
import client.shared.Utils;
import client.handlers.*;
import client.protocol.Choice;
import client.protocol.Command;
import client.protocol.payloads.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;

public class ChatCli implements ServerListener {

    private final Scanner scanner = new Scanner(System.in);
    private final Client client;
    private final ArrayList<User> users = new ArrayList<>();
    private String currentUsername = "";
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<Command, Consumer<String>> consumerMap = new HashMap<>();
    private final Map<String, FileTransfer> receivedFtr = new HashMap<>();
    private final List<OutgoingFileTransfer> sentFtr = new ArrayList<>();
    private final Map<String, FileReceiveSocket> fileReceiveSockets = new HashMap<>();
    private final Map<String, FileTransferSocket> fileTransferSockets = new HashMap<>();

    public ChatCli(String ip, int port) {
        consumerMap.put(Command.READY, new ReadyHandler(this));
        consumerMap.put(Command.ENTER_RESP, new EnterRespHandler(this));
        consumerMap.put(Command.BROADCAST_RESP, new StatusHandler(this, "Message sent successfully", "Failed to send message"));
        consumerMap.put(Command.BROADCAST, new BroadcastHandler(this));
        consumerMap.put(Command.JOINED, new JoinedHandler());
        consumerMap.put(Command.LEFT, new LeftHandler());
        consumerMap.put(Command.HANGUP, new HangupHandler(this));
        consumerMap.put(Command.PONG_ERROR, new PongErrorHandler());
        consumerMap.put(Command.BYE_RESP, new ByeResponseHandler(this));
        consumerMap.put(Command.PING, new PingHandler(this));
        consumerMap.put(Command.UNKNOWN_COMMAND, new UnknownCommandHandler());
        consumerMap.put(Command.PARSE_ERROR, new ParseErrorHandler());
        consumerMap.put(Command.LIST_USERS_RESP, new ListUsersHandler(this));
        consumerMap.put(Command.PM, new PmHandler(this));
        consumerMap.put(Command.PM_RESP, new StatusHandler(this, "Private Message sent successfully", "Error sending PM"));
        consumerMap.put(Command.RPS_REQ_RESP, new StatusHandler(this, "Invite sent successfully", "Failed to invite user"));
        consumerMap.put(Command.RPS_INVITE, new RpsInviteHandler(this));
        consumerMap.put(Command.RPS_ACCEPT_RESP,  new StatusHandler(this, "Invite has been accepted", "Failed to accept invite"));
        consumerMap.put(Command.RPS_DECLINE_RESP, new StatusHandler(this, "Invite has been declined", "Failed to decline invite"));
        consumerMap.put(Command.RPS_DECLINE, new RpsDeclineHandler(this));
        consumerMap.put(Command.RPS_START, new RpsStartHandler(this));
        consumerMap.put(Command.RPS_CHOICE_RESP, new StatusHandler(this, "Choice has been sent", "Failed to send choice"));
        consumerMap.put(Command.RPS_TIMEOUT, new RpsTimeoutHandler());
        consumerMap.put(Command.RPS_RESULT, new RpsResultHandler(this));
        consumerMap.put(Command.FILE_TRANS_RESP, new StatusHandler(this, "File transfer request sent successfully", "Failed to send file transfer request"));
        consumerMap.put(Command.FILE_TRANS, new FileTransferHandler(this));
        consumerMap.put(Command.FILE_TRANS_ACCEPT_RESP, new FileTransferAcceptResponseHandler(this));
        consumerMap.put(Command.FILE_TRANS_DECLINE_RESP, new FileTransferDeclineResponseHandler(this));
        consumerMap.put(Command.FILE_TRANS_ACCEPT, new FileTransferAcceptHandler(this));
        consumerMap.put(Command.FILE_TRANS_DECLINE, new FileTransferDeclineHandler(this));
        consumerMap.put(Command.FILE_TRANS_BEGIN, new FileTransBeginHandler(this));
        consumerMap.put(Command.FILE_TRANS_DONE_RESP, new StatusHandler(this, "File transfer finished successfully", "The file transfer could not be finished"));
        consumerMap.put(Command.FILE_TRANS_DONE, new FileTransDoneHandler(this));
        consumerMap.put(Command.FILE_TRANS_CANCEL, new FileTransCancelHandler(this));
        client = new Client(ip, port, this);
    }

    @Override
    public void onServerMessage(String command, String json) {
        Consumer<String> consumer = consumerMap.get(Command.valueOf(command));
        if(consumer != null) {
            consumer.accept(json);
        } else {
            Printer.printLineColour("Unknown command: " + command, Printer.ConsoleColour.RED);
        }
    }

    public void requestUsername() {
        Printer.printColourBold("Please enter your username: ", Printer.ConsoleColour.WHITE);
        currentUsername = scanner.nextLine().trim();
        try {
            client.sendPackage(Command.ENTER, mapper.writeValueAsString(new Username(currentUsername)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public List<User> getUsers() {
        return users;
    }

    public Client getClient() {
        return client;
    }

    public List<OutgoingFileTransfer> getSentFtr() {
        return sentFtr;
    }

    public Map<String, FileReceiveSocket> getFileReceiveSockets() {
        return fileReceiveSockets;
    }

    public Map<String, FileTransferSocket> getFileTransferSockets() {
        return fileTransferSockets;
    }

    public Map<String, FileTransfer> getReceivedFtr() {
        return receivedFtr;
    }

    public OutgoingFileTransfer findSentFileTransferRequest(String receiverUsername, String checksum) {
        OutgoingFileTransfer outgoingFileTransfer = null;
        for (OutgoingFileTransfer oft : sentFtr) {
            if (oft.receiverUsername().equals(receiverUsername) && oft.checksum().equals(checksum)) {
                outgoingFileTransfer = oft;
                break;
            }
        }
        return outgoingFileTransfer;
    }

    public void writeToServer() {
        try {
            String message;
            while ((message = scanner.nextLine()) != null) {
                if (message.equalsIgnoreCase(CliCommands.COMMAND_EXIT)) {
                    client.sendPackage(Command.BYE, null);
                    client.closeConnection();
                    Printer.printLineColour("You've logged out. CYA NEXT TIME!", Printer.ConsoleColour.RED);
                    break;
                } else if (message.equalsIgnoreCase(CliCommands.COMMAND_HELP)) {
                    printHelp();
                } else if (message.equalsIgnoreCase(CliCommands.COMMAND_LIST_USERS)) {
                    client.sendPackage(Command.LIST_USERS_REQ, null);
                } else if (CliCommands.PATTERN_PM.matcher(message).matches()) {
                    String username = message.split(" ")[1];
                    Matcher pmMessageMatcher = CliCommands.PATTERN_PM_MESSAGE.matcher(message);
                    pmMessageMatcher.find();
                    String pmMessage = pmMessageMatcher.group();
                    pmMessage = pmMessage.substring(1, pmMessage.length() - 1);

                    client.sendPackage(Command.PM_REQ, mapper.writeValueAsString(new PrivateMessage(username, pmMessage)));
                } else if (CliCommands.PATTERN_RPS.matcher(message).matches()) {
                    String username = message.split(" ")[1];
                    client.sendPackage(Command.RPS_REQ, mapper.writeValueAsString(new Username(username)));
                } else if (CliCommands.PATTERN_ACCEPT.matcher(message).matches()) {
                    String username = message.split(" ")[1];
                    client.sendPackage(Command.RPS_ACCEPT, mapper.writeValueAsString(new Username(username)));
                } else if (CliCommands.PATTERN_DECLINE.matcher(message).matches()) {
                    String username = message.split(" ")[1];
                    client.sendPackage(Command.RPS_DECLINE, mapper.writeValueAsString(new Username(username)));
                } else if (CliCommands.PATTERN_CHOICE.matcher(message).matches()) {
                    handleRpsChoice(message);
                } else if (CliCommands.PATTERN_FILE_TRANSFER.matcher(message).matches()) {
                    // Calculating the file hash can take a long time, so we handle this on a separate thread so that chatting stays possible
                    final String finalMessage = message;
                    new Thread(() -> handleFileTransfer(finalMessage)).start();
                } else if (CliCommands.PATTERN_ACCEPT_FILE.matcher(message).matches() || CliCommands.PATTERN_DECLINE_FILE.matcher(message).matches()) {
                    handleFtrResponse(message);
                } else {
                    if (!message.isBlank()) {
                        client.sendPackage(Command.BROADCAST_REQ, mapper.writeValueAsString(new BroadcastRequest(message)));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            Printer.printLineColour("There was a problem handling your command", Printer.ConsoleColour.RED);
        } finally {
            scanner.close();
        }
    }

    private void printHelp() {
        Printer.printLineColour("Available commands:", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_HELP + ": Show this menu", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_EXIT + ": Log out and disconnect from the server", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_LIST_USERS + ": List the users that are currently logged in", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_PM + " [username] \"[message]\": Send a private message to [username]", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_RPS + " [username]: Challenge [username] to a game of rock, paper, scissors", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_ACCEPT + " [username]: Accept the invitation of [username] to a game of rock, paper, scissors", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_DECLINE + " [username]: Decline the invitation of [username] to a game of rock, paper, scissors\"", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_CHOICE + " [rock|paper|scissors]: Choose a move during a rock, paper, scissors game", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_FILE_TRANSFER + " [username] \"[file_path]\": Request to send a file to [username]", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_ACCEPT_FILE + " [uuid]: Accept the file transfer with the uuid [uuid]", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- " + CliCommands.COMMAND_DECLINE_FILE + " [uuid]: Decline the file transfer with the uuid [uuid]", Printer.ConsoleColour.CYAN);
        Printer.printLineColour("\t- [message]: Send a broadcast message to all users", Printer.ConsoleColour.CYAN);
    }

    private void handleRpsChoice(String message) throws JsonProcessingException {
        String choiceString = message.split(" ")[1];
        Choice choice;
        if (choiceString.equalsIgnoreCase(CliCommands.COMMAND_ROCK)) {
            choice = Choice.ROCK;
        } else if (choiceString.equalsIgnoreCase(CliCommands.COMMAND_SCISSORS)) {
            choice = Choice.SCISSORS;
        } else if (choiceString.equalsIgnoreCase(CliCommands.COMMAND_PAPER)) {
            choice = Choice.PAPER;
        } else {
            Printer.printLineColour("Invalid choice: " + choiceString, Printer.ConsoleColour.RED);
            return;
        }
        client.sendPackage(Command.RPS_CHOICE, mapper.writeValueAsString(new RpsChoice(choice)));
    }

    private void handleFileTransfer(String message) {
        try {
            Printer.printLineColour("Calculating file hash...", Printer.ConsoleColour.BLUE);

            String[] split = message.split(" ", 3);
            String username = split[1];
            String filePath = split[2].substring(1, split[2].length() - 1);
            File file = new File(filePath);
            if (!file.exists()) {
                Printer.printLineColour("File \"" + filePath + "\" does not exist", Printer.ConsoleColour.RED);
                return;
            }
            Path path = Paths.get(filePath);
            long fileSize = Files.size(path);


            String fileHash = Utils.getFileHash(file);

            FileTransferRequest fileTransferRequest = new FileTransferRequest(username, file.getName(), fileSize, fileHash);
            sentFtr.add(new OutgoingFileTransfer(username, path, fileHash, null));
            client.sendPackage(Command.FILE_TRANS_REQ, mapper.writeValueAsString(fileTransferRequest));
        } catch (IOException | NoSuchAlgorithmException e) {
            Printer.printLineColour("Error sending file", Printer.ConsoleColour.RED);
        }
    }

    private void handleFtrResponse(String message) throws JsonProcessingException {
        String[] split = message.split(" ", 2);
        FileTransfer fileTransfer = receivedFtr.get(split[1]);
        if(fileTransfer == null) {
            Printer.printLineColour("No request with this uuid found", Printer.ConsoleColour.RED);
            return;
        }
        if (split[0].equalsIgnoreCase(CliCommands.COMMAND_ACCEPT_FILE)) {
            client.sendPackage(Command.FILE_TRANS_ACCEPT_REQ, mapper.writeValueAsString(new UUIDPayload(fileTransfer.uuid())));
        } else if (split[0].equalsIgnoreCase(CliCommands.COMMAND_DECLINE_FILE)) {
            client.sendPackage(Command.FILE_TRANS_DECLINE_REQ, mapper.writeValueAsString(new UUIDPayload(fileTransfer.uuid())));
        }
    }
}