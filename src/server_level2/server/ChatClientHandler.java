package server_level2.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import server_level2.handlers.*;
import server_level2.protocol.Choice;
import server_level2.protocol.Command;
import server_level2.protocol.payloads.*;
import server_level2.shared.Consts;
import server_level2.shared.Printer;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ChatClientHandler extends Thread{

    private static final int PING_PERIOD = 10;

    private final Socket socket;
    private final Server server;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private ScheduledExecutorService pingThreadPool;
    private ScheduledFuture<?> pingFuture;
    private DisconnectionListener disconnectionListener;
    private boolean connected = true;
    private boolean awaitingPong = false;

    private final Map<Command, Consumer<String>> consumerMap = new HashMap<>();

    private User user = null;
    private String rpsGamePartner = null;
    private Choice choice = null;
    private final ScheduledExecutorService rpsExecutorService;
    private ScheduledFuture<?> rpsFuture;

    public ChatClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        rpsExecutorService = Executors.newScheduledThreadPool(1);

        consumerMap.put(Command.PONG, new PongHandler(this));
        consumerMap.put(Command.ENTER, new EnterHandler(this));
        consumerMap.put(Command.BYE, new ByeHandler(this));
        consumerMap.put(Command.BROADCAST_REQ, new BroadcastRequestHandler(this));
        consumerMap.put(Command.LIST_USERS_REQ, new ListUsersHandler(this));
        consumerMap.put(Command.PM_REQ, new PmRequestHandler(this));
        consumerMap.put(Command.RPS_REQ, new RpsRequestHandler(this));
        consumerMap.put(Command.RPS_ACCEPT, new RpsAcceptHandler(this));
        consumerMap.put(Command.RPS_DECLINE, new RpsDeclineHandler(this));
        consumerMap.put(Command.RPS_CHOICE, new RpsChoiceHandler(this));
        consumerMap.put(Command.FILE_TRANS_REQ, new FileTransferRequestHandler(this));
        consumerMap.put(Command.FILE_TRANS_ACCEPT_REQ, new FileTransferAcceptRequestHandler(this));
        consumerMap.put(Command.FILE_TRANS_DECLINE_REQ, new FileTransferDeclineRequestHandler(this));
        consumerMap.put(Command.FILE_TRANS_DONE, new FileTransferDoneHandler(this));
    }

    public void sendToClient(Payload payload) {
        try {
            sendToClient(payload.getCommand(), getMapper().writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        try {
            Printer.printLineColour(user, "Disconnecting...", Printer.ConsoleColour.RED);
            if(disconnectionListener != null) {
                disconnectionListener.onDisconnect();
            }
            reader.close();
            writer.close();
            socket.close();
            connected = false;
            interrupt();
            if (pingThreadPool != null) {
                pingThreadPool.close();
            }
            user = null;
        } catch (IOException e) {
            Printer.printLineColour(user, "Error disconnecting...", Printer.ConsoleColour.RED);
        }
    }

    public ObjectMapper getMapper() {
        return server.getMapper();
    }

    public Server getServer() {
        return server;
    }

    public User getUser() {
        return user;
    }

    public boolean isAwaitingPong() {
        return awaitingPong;
    }

    public void pongReceived() {
        awaitingPong = false;
        pingFuture.cancel(false);
    }

    public void logInUser(User user) {
        if (this.user == null) {
            server.sendToAllExcept(user.username(), new Joined(user.username()));
            this.user = user;

            pingThreadPool = Executors.newScheduledThreadPool(1);
            pingThreadPool.scheduleAtFixedRate(this::sendPing, PING_PERIOD, PING_PERIOD, TimeUnit.SECONDS);
        }
    }

    public void setDisconnectionListener(DisconnectionListener disconnectionListener) {
        this.disconnectionListener = disconnectionListener;
    }

    public void sendParseError() {
        sendToClient(Command.PARSE_ERROR, null);
    }

    public String getRpsGamePartner() {
        return rpsGamePartner;
    }

    public Choice getRpsChoice() {
        return choice;
    }

    public void startGameWith(String rpsGamePartner) {
        this.rpsGamePartner = rpsGamePartner;
        sendToClient(new RpsStart(rpsGamePartner));
        rpsFuture = rpsExecutorService.schedule(() -> endGame(null), 60, TimeUnit.SECONDS);
    }

    public void setRpsChoice(Choice choice) {
        this.choice = choice;
    }

    public void endGame(RpsResult result) {
        if (result != null) {
            sendToClient(result);
        } else {
            sendToClient(Command.RPS_TIMEOUT, null);
        }
        if(rpsFuture != null) {
            rpsFuture.cancel(true);
            rpsFuture = null;
        }
        rpsGamePartner = null;
        choice = null;
    }

    @Override
    public void run() {
        try {
            // Send ready to client
            sendToClient(new Ready(Consts.SERVER_VERSION));

            String message;
            while ((message = reader.readLine()) != null && connected) {
                Printer.printLineColour(user, "RECEIVED: " + message, Printer.ConsoleColour.WHITE);
                String[] splitMessage = message.split(" ", 2);
                String json = null;
                if (splitMessage.length > 1) {
                    json = splitMessage[1];
                }

                try {
                    Command command = Command.valueOf(splitMessage[0]);
                    consumerMap.get(command).accept(json);
                } catch (IllegalArgumentException e) {
                    Printer.printLineColour(getUser(), "Unknown command: " + splitMessage[0], Printer.ConsoleColour.RED);
                    sendToClient(Command.UNKNOWN_COMMAND, null);
                }
            }
            Printer.printLineColour(user, "Connection to client lost", Printer.ConsoleColour.RED);
        } catch (IOException e) {
            Printer.printLineColour(user, "Connection to client lost. Reason: " + e.getMessage(), Printer.ConsoleColour.RED);
        } finally {
            if(connected) {
                disconnect();
            }
        }
    }

    private void sendPing() {
        Printer.printLineColour(user, "Sending ping...", Printer.ConsoleColour.YELLOW);
        pingFuture = pingThreadPool.schedule(() -> {
            Printer.printLineColour(user, "Did not receive pong in time", Printer.ConsoleColour.RED);
            sendToClient(new Hangup(7000));
            disconnect();
        }, 3, TimeUnit.SECONDS);
        awaitingPong = true;
        sendToClient(Command.PING, null);
    }

    private void sendToClient(Command command, String json) {
        String data;
        if (json != null) {
            data = command + " " + json;
        } else {
            data = command.toString();
        }
        Printer.printLineColour(user, "SENDING: " + data, Printer.ConsoleColour.WHITE);
        writer.println(data);
    }
}