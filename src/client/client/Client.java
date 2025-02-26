package client.client;

import client.shared.Printer;
import client.protocol.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private String ip;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ServerListener serverListener;
    private boolean isExiting = false; // track if the client is exiting

    public Client(String ip, int port, ServerListener serverListener) {
        try {
            this.ip = ip;
            socket = new Socket(ip, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.serverListener = serverListener;

            new Thread(this::readFromServer).start();

            Printer.printLineColour("Connected to the server at " + ip + ":" + port, Printer.ConsoleColour.GREEN);
        } catch (IOException e) {
            Printer.printLineColour("Could not connect to server: " + e.getMessage(), Printer.ConsoleColour.RED);
        }
    }

    public void sendPackage(Command command, String json) {
        if (json != null) {
            writer.println(command + " " + json);
        } else {
            writer.println(command);
        }
    }

    private void readFromServer() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (isExiting) break; // stop reading from the server if the client is exiting
                String[] splitMessage = message.split(" ", 2);
                String json = null;
                if (splitMessage.length > 1) {
                    json = splitMessage[1];
                }
                serverListener.onServerMessage(splitMessage[0], json);
            }
            Printer.printLineColour("Connection to server lost", Printer.ConsoleColour.RED);
            System.exit(1);
        } catch (IOException e) {
            if (!isExiting) { // only show if connection lost unexpectedly
                Printer.printLineColour("Connection to server lost. Reason: " + e.getMessage(), Printer.ConsoleColour.RED);
            }
        }
    }

    public void closeConnection() {
        isExiting = true;
        try {
            if (socket != null) socket.close();
            Printer.printLineColour("Connection closed.", Printer.ConsoleColour.RED);
        } catch (IOException e) {
            Printer.printLineColour("Error closing connection.", Printer.ConsoleColour.RED);
        }
    }

    public String getIp() {
        return ip;
    }
}
