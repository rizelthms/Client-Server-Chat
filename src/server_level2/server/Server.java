package server_level2.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;
import server_level2.protocol.Choice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ServerSocket serverSocket;
    private final ServerSocket fileTransferSocket;
    private final List<ChatClientHandler> clients = new ArrayList<>();

    private final List<Invite> invites = new ArrayList<>();

    private final Map<String, ServerFileTransferRequest> unansweredFtr = new HashMap<>();
    private final List<ServerFileTransferRequest> ongoingFileTransfers = new ArrayList<>();

    public Server(int port, int fileTransferPort) {
        try {
            Printer.printLineColour("Starting Server on port " + port + " and " + fileTransferPort, Printer.ConsoleColour.GREEN);
            serverSocket = new ServerSocket(port);
            fileTransferSocket = new ServerSocket(fileTransferPort);
            new Thread(this::acceptClients).start();
            new Thread(this::acceptFileTransferSockets).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getUsers() {
        return clients.stream()
                .map(ChatClientHandler::getUser)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean isUserLoggedIn(String username) {
        return clients.stream()
                .map(ChatClientHandler::getUser)
                .filter(Objects::nonNull)
                .anyMatch(user -> user.username().equals(username));
    }

    public void sendToAllExcept(String userException, Payload payload) {
        clients.stream()
                .filter(client -> client.getUser() != null)
                .filter(client -> !Objects.equals(client.getUser().username(), userException))
                .forEach(client -> client.sendToClient(payload));
    }

    public void sendToUser(String username, Payload payload) {
        clients.stream()
                .filter(client -> client.getUser() != null)
                .filter(client -> client.getUser().username().equals(username))
                .findFirst()
                .ifPresentOrElse(
                        client -> client.sendToClient(payload),
                        () -> Printer.printLineColour("User " + username + " not found on the server", Printer.ConsoleColour.RED)
                );
    }

    public boolean isUserInGame(String username) {
        return clients.stream()
                .filter(client -> client.getUser() != null)
                .filter(client -> client.getUser().username().equals(username))
                .findFirst()
                .filter(client -> client.getRpsGamePartner() != null)
                .isPresent();
    }

    public void startRpsGame(String user1, String user2) {
        ChatClientHandler client1 = getClientOfUser(user1);
        ChatClientHandler client2 = getClientOfUser(user2);
        if (client1 == null || client2 == null) {
            Printer.printLineColour("Could not start a game one of the users does not exist", Printer.ConsoleColour.RED);
            return;
        }
        if (client1.getRpsGamePartner() != null || client2.getRpsGamePartner() != null) {
            Printer.printLineColour("Could not start a game because one of the users is already in a game", Printer.ConsoleColour.RED);
            return;
        }
        removeInvite(user1, user2);
        client1.startGameWith(user2);
        client2.startGameWith(user1);
    }

    public void removeInvite(String user1, String user2) {
        invites.removeIf(invite -> (invite.fromUsername().equals(user1) && invite.toUsername().equals(user2)) ||
                invite.toUsername().equals(user1) && invite.fromUsername().equals(user2));
    }

    public void sendRpsInvite(String fromUser, String toUser) {
        sendToUser(toUser, new RpsInvite(fromUser));
        invites.add(new Invite(fromUser, toUser));
    }

    public boolean doesInviteExist(String from, String to) {
        return invites.stream().anyMatch(invite -> invite.fromUsername().equals(from) && invite.toUsername().equals(to));
    }

    private ChatClientHandler getClientOfUser(String username) {
        return clients.stream()
                .filter(client -> client.getUser() != null)
                .filter(client -> client.getUser().username().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void handleChoiceDone(ChatClientHandler client) {
        ChatClientHandler partnerClient = getClientOfUser(client.getRpsGamePartner());
        Choice choice = client.getRpsChoice();
        Choice partnerChoice = partnerClient.getRpsChoice();
        if (partnerChoice != null) {
            client.endGame(new RpsResult(choice.getResult(partnerChoice), partnerChoice));
            partnerClient.endGame(new RpsResult(partnerChoice.getResult(choice), choice));
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public Map<String, ServerFileTransferRequest> getUnansweredFtr() {
        return unansweredFtr;
    }

    public List<ServerFileTransferRequest> getOngoingFileTransfers() {
        return ongoingFileTransfers;
    }

    public void closeServer() {
        try {
            serverSocket.close();
            fileTransferSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancelFileTransfer(ServerFileTransferRequest ftr) {
        sendToUser(ftr.getReceiverUsername(), new FileTransferCancel(ftr.getReceiverUUID()));
        sendToUser(ftr.getSenderUsername(), new FileTransferCancel(ftr.getSenderUUID()));
        ftr.getReceiverHandler().disconnect();
        ftr.getSenderHandler().disconnect();
        ongoingFileTransfers.remove(ftr);
    }

    private void acceptClients() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                Printer.printLineColour("New client connected: " + socket.getInetAddress() + ":" + socket.getPort(), Printer.ConsoleColour.CYAN);

                ChatClientHandler handler = new ChatClientHandler(socket, this);
                handler.setDisconnectionListener(() -> {
                    clients.remove(handler);

                    // stop file transfer if any are ongoing
                    if (handler.getUser() != null) {
                        for (ServerFileTransferRequest ftr : ongoingFileTransfers) {
                            if (ftr.getSenderUsername().equals(handler.getUser().username()) || ftr.getReceiverUsername().equals(handler.getUser().username())) {
                                cancelFileTransfer(ftr);
                            }
                        }
                    }
                });
                clients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            Printer.printLineColour("Server socket closed", Printer.ConsoleColour.RED);
        }
    }

    private void acceptFileTransferSockets() {
        try {
            while (!fileTransferSocket.isClosed()) {
                Socket socket = fileTransferSocket.accept();

                Printer.printLineColour("New File transfer client connected: " + socket.getInetAddress() + ":" + socket.getPort(), Printer.ConsoleColour.CYAN);
                FileTransferHandler handler = getFileTransferHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            Printer.printLineColour("File transfer socket closed", Printer.ConsoleColour.RED);
        }
    }

    private FileTransferHandler getFileTransferHandler(Socket socket) {
        FileTransferHandler handler = new FileTransferHandler(socket, this);
        handler.setDisconnectionListener(() -> {
            // on disconnect remove file transfer from ongoing transfers
            if (handler.getUUID() != null) {
                ongoingFileTransfers.removeIf(ftr -> ftr.getSenderUUID().equals(handler.getUUID()) || ftr.getReceiverUUID().equals(handler.getUUID()));
            }
        });
        return handler;
    }
}