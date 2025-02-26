
# Client-Server Chat Application

## Overview

This project is a Client-Server Chat Application implemented in Java. It supports various features such as broadcasting messages, private messaging, file transfer, and playing a game of Rock-Paper-Scissors. The application uses a custom protocol to handle communication between the client and the server.

## Features

- **Broadcast Messages**: Send messages to all connected users.
- **Private Messaging**: Send private messages to specific users.
- **File Transfer**: Send and receive files between users.
- **Rock-Paper-Scissors Game**: Challenge other users to a game of Rock-Paper-Scissors.
- **User Management**: List, join, and leave the chat.

## Protocol Commands

- `ENTER`: Enter the chat with a username.
- `BROADCAST_REQ`: Request to broadcast a message.
- `BROADCAST`: Broadcast a message to all users.
- `PM_REQ`: Request to send a private message.
- `PM`: Send a private message to a specific user.
- `RPS_REQ`: Request to challenge a user to Rock-Paper-Scissors.
- `RPS_ACCEPT`: Accept a Rock-Paper-Scissors challenge.
- `RPS_DECLINE`: Decline a Rock-Paper-Scissors challenge.
- `RPS_CHOICE`: Send a choice (rock, paper, or scissors) for the game.
- `FILE_TRANS_REQ`: Request to transfer a file.
- `FILE_TRANS_ACCEPT`: Accept a file transfer request.
- `FILE_TRANS_DECLINE`: Decline a file transfer request.
- `FILE_TRANS_BEGIN`: Begin file transfer.
- `FILE_TRANS_DONE`: Notify that file transfer is done.
- `BYE`: Disconnect from the server.

## Usage

1. **Start the Server**: Run the server application to start listening for client connections.
2. **Connect the Client**: Run the client application and connect to the server using the server's IP address and port.
3. **Enter Username**: Enter a username to join the chat.
4. **Use Commands**: Use the available commands to interact with other users.

## Commands

- `help`: Show the help menu.
- `exit`: Log out and disconnect from the server.
- `list_users`: List the users that are currently logged in.
- `pm [username] "[message]"`: Send a private message to a user.
- `rps [username]`: Challenge a user to Rock-Paper-Scissors.
- `accept [username]`: Accept a Rock-Paper-Scissors challenge.
- `decline [username]`: Decline a Rock-Paper-Scissors challenge.
- `choice [rock|paper|scissors]`: Choose a move during Rock-Paper-Scissors.
- `transfer_file [username] "[file_path]"`: Request to send a file to a user.
- `accept_file [uuid]`: Accept a file transfer request.
- `decline_file [uuid]`: Decline a file transfer request.

## Requirements

- Java 8 or higher
- Internet connection for client-server communication

## Setup

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/Client-Server-Chat.git
    ```
2. Navigate to the project directory:
    ```sh
    cd Client-Server-Chat
    ```
3. Compile the project:
    ```sh
    javac -d bin src/**/*.java
    ```
4. Run the server:
    ```sh
    java -cp bin server.Server
    ```
5. Run the client:
    ```sh
    java -cp bin client.cli.ChatCli
    ```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

