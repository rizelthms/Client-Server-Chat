# Protocol Description

This client-server protocol describes the following scenarios:

- Setting up a connection between client and server.
- Broadcasting a message to all connected clients.
- Periodically sending heartbeat to connected clients.
- Disconnection from the server.
- Handling invalid messages.

In the description below, `C -> S` represents a message from the client `C` is send to server `S`. When applicable, `C`
is extended with a number to indicate a specific client, e.g., `C1`, `C2`, etc. The keyword `others` is used to indicate
all other clients except for the client who made the request. Messages can contain a JSON body. Text shown between `<`
and `>` are placeholders.

The protocol follows the formal JSON specification, RFC 8259, available on https://www.rfc-editor.org/rfc/rfc8259.html

# 1. Establishing a connection

The client first sets up a socket connection to which the server responds with a welcome message. The client supplies a
username on which the server responds with an OK if the username is accepted or an ERROR with a number in case of an
error.
_Note:_ A username may only consist of characters, numbers, and underscores ('_') and has a length between 3 and 14
characters.

## 1.1 Happy flow

Client sets up the connection with server.

```
S -> C: READY {"version": "<server version number>"}
```

- `<server version number>`: the semantic version number of the server.

After a while when the client logs the user in:

```
C -> S: ENTER {"username":"<username>"}
S -> C: ENTER_RESP {"status":"OK"}
```

- `<username>`: the username of the user that needs to be logged in.
  To other clients (Only applicable when working on Level 2):

```
S -> others: JOINED {"username":"<username>"}
```

## 1.2 Unhappy flow

```
S -> C: ENTER_RESP {"status":"ERROR", "code":<error code>}
```      

Possible `<error code>`:

| Error code | Description                              |
|------------|------------------------------------------|
| 5000       | User with this name already exists       |
| 5001       | Username has an invalid format or length |      
| 5002       | Already logged in                        |

# 2. Broadcast message

Sends a message from a client to all other clients. The sending client does not receive the message itself but gets a
confirmation that the message has been sent.

## 2.1 Happy flow

```
C -> S: BROADCAST_REQ {"message":"<message>"}
S -> C: BROADCAST_RESP {"status":"OK"}
```

- `<message>`: the message that must be sent.

Other clients receive the message as follows:

```
S -> others: BROADCAST {"username":"<username>","message":"<message>"}   
```   

- `<username>`: the username of the user that is sending the message.

## 2.2 Unhappy flow

```
S -> C: BROADCAST_RESP {"status": "ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description           |
|------------|-----------------------|
| 6000       | User is not logged in |

# 3. Heartbeat message

Sends a ping message to the client to check whether the client is still active. The receiving client should respond with
a pong message to confirm it is still active. If after 3 seconds no pong message has been received by the server, the
connection to the client is closed. Before closing, the client is notified with a HANGUP message, with reason code 7000.

The server sends a ping message to a client every 10 seconds. The first ping message is send to the client 10 seconds
after the client is logged in.

When the server receives a PONG message while it is not expecting one, a PONG_ERROR message will be returned.

## 3.1 Happy flow

```
S -> C: PING
C -> S: PONG
```     

## 3.2 Unhappy flow

```
S -> C: HANGUP {"reason": <reason code>}
[Server disconnects the client]
```      

Possible `<reason code>`:

| Reason code | Description      |
|-------------|------------------|
| 7000        | No pong received |    

```
S -> C: PONG_ERROR {"code": <error code>}
```

Possible `<error code>`:

| Error code | Description       |
|------------|-------------------|
| 8000       | Pong without ping |    

# 4. Termination of the connection

When the connection needs to be terminated, the client sends a bye message. This will be answered (with a BYE_RESP
message) after which the server will close the socket connection.

## 4.1 Happy flow

```
C -> S: BYE
S -> C: BYE_RESP {"status":"OK"}
[Server closes the socket connection]
```

Other, still connected clients, clients receive:

```
S -> others: LEFT {"username":"<username>"}
```

## 4.2 Unhappy flow

- None

# 5. Invalid message header

If the client sends an invalid message header (not defined above), the server replies with an unknown command message.
The client remains connected.

Example:

```
C -> S: MSG This is an invalid message
S -> C: UNKNOWN_COMMAND
```

# 6. Invalid message body

If the client sends a valid message, but the body is not valid JSON, the server replies with a pars error message. The
client remains connected.

Example:

```
C -> S: BROADCAST_REQ {"aaaa}
S -> C: PARSE_ERROR
```

# 7. Listing currently connected users

## 7.1 Happy flow

The client can get a list of the currently connected users by sending a LIST_USERS_REQ message.
The server will respond with a list of all users that are currently connected.

Example:

```
C -> S: LIST_USERS_REQ
S -> C: LIST_USERS_RESP {"users":[{"username":"<username1>"}, {"username":"<username2>"}, {"username":"<username3>"}]}
```

## 7.2 Unhappy flow

```
C -> S: LIST_USERS_REQ
S -> C: LIST_USERS_RESP {"status": "ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description           | 
|------------|-----------------------|
| 6000       | User is not logged in |

# 8. Private messages

Sends a private message to a specific user. The sending client gets a confirmation the message has been sent or an error
code if something went wrong. The message will only be sent to the specified user.

## 8.1 Happy flow

```
C -> S: PM_REQ {"username":"<recipient_username>", "message":"<message>"}
S -> C: PM_RESP {"status":"OK"}
```

The message is only sent to the user that was specified by the sender with the `<recipient_username>` field:

```  
S -> C: PM {"username":"<sender_username>","message":"<message>"}
```  

- `<sender_username>`: the username of the user that is sending the message.

## 8.2 Unhappy flow

```  
S -> C: PM_RESP {"status": "ERROR", "code": <error code>}  
```  

Possible `<error code>`:

| Error code | Description                                                     |  
|------------|-----------------------------------------------------------------|  
| 9000       | User sending the message is not logged in                       |
| 9001       | The user the message is supposed to be sent to is not logged in |

# 9. Rock, paper, scissors

## 9.1 Invitation

### 9.1.1 Happy flow

```  
C -> S: RPS_REQ {"username":"<username>"}
S -> C: RPS_REQ_RESP {"status":"OK"}
```

The user may send invitations to multiple users, as long as no game is ongoing:

```  
C -> S: RPS_REQ {"username":"<username1>"}
S -> C: RPS_REQ_RESP {"status":"OK"}
C -> S: RPS_REQ {"username":"<username2>"}
S -> C: RPS_REQ_RESP {"status":"OK"}
C -> S: RPS_REQ {"username":"<username3>"}
S -> C: RPS_REQ_RESP {"status":"OK"}
```

### 9.1.2 Unhappy flow

```  
C -> S: RPS_REQ {"username":"<username>"}
S -> C: RPS_REQ_RESP {"status":"ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description                                                        |  
|------------|--------------------------------------------------------------------|  
| 6000       | You are not logged in                                              |
| 10001      | The user that is challenged is not logged in                       |
| 10002      | The user that is challenged is already in a game with someone else |
| 10003      | You are already in a game with someone else                        |
| 10004      | You tried to invite yourself to a game                             |


## 9.2 Response to invitation

### 9.2.1 Happy flow

Users that are challenged to a game receive an RPS_INVITE message and can either accept or decline. 
Since you can receive invitations from multiple users before you reply, you have to specify which 
invitation you want to respond to by sending a username.

Accepting:
```  
S -> C: RPS_INVITE {"username":"<username1>"}
C -> S: RPS_ACCEPT {"username":"<username2>"}
S -> C: RPS_ACCEPT_RESP {"status":"OK"}
```

- `<username1>`: the username of the player that invited you
- `<username2>`: the username of the player whose invitation you're accepting (in case you have multiple)

Declining:
```  
S -> C: RPS_INVITE {"username":"<username1>"}
C -> S: RPS_DECLINE {"username":"<username2>"}
S -> C: RPS_DECLINE_RESP {"status":"OK"}
```

- `<username1>`: the username of the player that invited you
- `<username2>`: the username of the player whose invitation you're declining (in case you have multiple)

The server will also send an RPS_DECLINE package to the user whose invitation was declined:
```
S -> C: RPS_DECLINE {"username":"<username>"}
```

- `<username>`: the username of the player that declined the invitation

### 9.2.2 Unhappy flow

Accepting:
```  
S -> C: RPS_INVITE {"username":"<username>"}
C -> S: RPS_ACCEPT {"username":"<username>"}
S -> C: RPS_ACCEPT_RESP {"status":"ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description                                                                   |  
|------------|-------------------------------------------------------------------------------|  
| 6000       | You are not logged in                                                         |
| 10006      | The user whose invite you accepted did not invite you                         |
| 10007      | The user whose invite you accepted is no longer logged in                     |
| 10008      | The user whose invite you accepted has since started a game with someone else |
| 10003      | You are already in a game with someone else                                   |

Declining:
```  
S -> C: RPS_INVITE {"username":"<username>"}
C -> S: RPS_DECLINE {"username":"<username>"}
S -> C: RPS_DECLINE_RESP {"status":"ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description                                           |  
|------------|-------------------------------------------------------|  
| 6000       | You are not logged in                                 |
| 10011      | The user whose invite you declined did not invite you |


## 9.3 Playing the game

Once the opponent accepted a game, the server will send an RPS_START package to both players to let them know the game
started and who their opponent is (in case multiple invitations were sent):

```
S -> C1: RPS_START {"username":"<username>"}
S -> C2: RPS_START {"username":"<username>"}
```
- `<username>`: the username of the opponent

Both players can then send their choice:

### 9.3.1 Happy flow

```  
C1 -> S: RPS_CHOICE {"choice": "<choice>"}
S -> C1: RPS_CHOICE_RESP {"status":"OK"}
C2 -> S: RPS_CHOICE {"choice": "<choice>"}
S -> C2: RPS_CHOICE_RESP {"status":"OK"}
```

- `<choice>`: Either "ROCK", "PAPER" or "SCISSORS".

### 9.3.2 Unhappy flow

```  
C -> S: RPS_CHOICE {"choice": "<choice>"}
S -> C: RPS_CHOICE_RESP {"status":"ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description                                        |  
|------------|----------------------------------------------------|  
| 6000       | You are not logged in                              |
| 10013      | No game is ongoing                                 |
| 10014      | Invalid choice (not "ROCK", "PAPER" or "SCISSORS") |

If your opponent does not send a move within 60 Seconds after the game started, the server will send an RPS_TIMEOUT message to the players and the game is considered ended:
```  
S -> C1: RPS_TIMEOUT
S -> C2: RPS_TIMEOUT
```
This will also cover the case of one player logging out during the game, so there is no specific error code for this case.

## 9.4 Results

After both players made their choice in time, the server sends the result and the choice of the other player to both players:

```
S -> C: RPS_RESULT {"result": "<result>", "otherChoice":"<choice>"}
```

- `<result>`: Either "WON", "LOST" or "TIE".
- `otherChoice`: Contains the <choice> of the other player.

# 10. File transfer

## 10.1 File transfer request

### 10.1.1 Happy flow

A user can request to send a file to another user by sending the username of the receiver, the name of the file and the size of the file in bytes in a FILE_TRANS_REQ to the server.

```  
C1 -> S: FILE_TRANS_REQ {"username":"<receiver_username>", "fileName":"<filename>", "fileSize":<file size>, "checksum":"<sha256hash>"}
S -> C1: FILE_TRANS_RESP {"status":"OK"}
```

The server will then send the request to the receiver with a UUID to identify it in the future:

```  
S -> C2:FILE_TRANS {"username":"<sender_username>", "filename":"<filename>", "filesize":<file size>, "checksum":"<sha256hash>", "uuid":"<receiver_uuid>"}
```

- `<receiver_uuid>`: A 128-bit UUID to identify the file request in future communications with the server

### 10.1.2 Unhappy flow

```  
C1 -> S: FILE_TRANS_REQ {"username":"<receiver_username>", "filename":"<filename>", "filesize":<file size>, "checksum":"<sha256hash>"}
S -> C1: FILE_TRANS_RESP {"status":"ERROR", "code":<error code>}
```

Possible `<error code>`:

| Error code | Description                        |  
|------------|------------------------------------|  
| 6000       | You are not logged in              |
| 11000      | The file receiver is not logged in |
| 11001      | Invalid file size (<= 0)           |
| 11002      | Invalid hash                       |
| 11003      | You can't send a file to yourself  |

## 10.2 File transfer response

### 10.2.1 Happy flow

When a user receives a file transfer request they can either accept or decline it. 
As a user can receive requests for multiple files from the same user the client has to send the UUID it received with the request to the server to identify which request it wants to accept or decline.

```  
S -> C2: FILE_TRANS {"username":"<sender_username>", "filename":"<filename>", "filesize":<file size>, "checksum":"<sha256hash>", "uuid":"<receiver_uuid>"}
C2 -> S: FILE_TRANS_ACCEPT_REQ {"uuid":"<receiver_uuid>"}
S -> C2: FILE_TRANS_ACCEPT_RESP {"status":"OK", "uuid":"<receiver_uuid>"}
```

```  
S -> C2:FILE_TRANS {"username":"<sender_username>", "filename":"<filename>", "filesize":<file size>, "checksum":"<sha256hash>", "uuid":"<receiver_uuid>"}
C2 -> S:FILE_TRANS_DECLINE_REQ {"uuid":"<receiver_uuid>"}
S -> C2: FILE_TRANS_DECLINE_RESP {"status":"OK", "uuid":"<receiver_uuid>"}
```
The server will then forward this to the user that initiated the file transfer along with another UUID to identify the sender of this file:
As the client could have sent multiple requests to send the same file to different users, the server also sends the username of the receiver and the checksum so the client knows which file transfer request is being replied to.

```  
S -> C1: FILE_TRANS_ACCEPT {"username":"<receiver_username>", "checksum":"<sha256hash>", "uuid":"<sender_uuid>"}
S -> C1: FILE_TRANS_DECLINE {"username":"<receiver_username>", "checksum":"<sha256hash>"}
```

- `<sender_uuid>`: A 128-bit UUID to identify the socket of the file sender

### 10.2.2 Unhappy flow

```  
C2 -> S: FILE_TRANS_ACCEPT_REQ {"uuid":"<receiver_uuid>"}
S -> C2: FILE_TRANS_ACCEPT_RESP {"status":"ERROR", "code":<error code>}
```

``` 
C2 -> S:FILE_TRANS_DECLINE_REQ {"uuid":"<receiver_uuid>"}
S -> C2: FILE_TRANS_DECLINE_RESP {"status":"ERROR", "code":<error code>}
```

Possible `<error code>`:

| Error code | Description                                           |
|------------|-------------------------------------------------------|  
| 6000       | You are not logged in                                 |
| 11004      | There is no request with the specified uuid           |
| 11005      | The user that sent the request is no longer logged in |

## 10.3 File transfer

### 10.3.1 Happy flow

After a file transfer has been accepted both clients open a new socket connection with the server on port 1338 and send their 16 uuid bytes over that socket.

```  
C1 -> S: <uuid_bytes>
C2 -> S: <uuid_bytes>
```

As the server should not buffer any file data and only forward it to the receiver, both sockets have to be open before the file transfer can begin.
To ensure this, the server sends a FILE_TRANS_BEGIN packet over the original socket (not the file transfer socket) to both clients with their UUIDs to let them know that the file transfer can begin.

```  
S -> C1: FILE_TRANS_BEGIN {"uuid":"<sender_uuid>"}
S -> C2: FILE_TRANS_BEGIN {"uuid":"<receiver_uuid>"}
```

After that the sender sends the file bytes over the file socket and the server forwards the bytes to the receiver socket.

### 10.3.2 Unhappy flow

If the uuid that a client sends is unknown to the server, the server simply closes the socket. 
As the server has no way of knowing which client the socket belongs to at this point, it can't inform the clients about their mistake.
However, the clients can see that there was a problem when the socket is closed without them receiving a FILE_TRANS_BEGIN packet.

## 10.4 File transfer end

### 10.4.1 Happy flow

Once the file transfer is finished the sender sends a FILE_TRANS_DONE packet over the original socket (not the file transfer socket) to the server.

```  
C1 -> S: FILE_TRANS_DONE {"uuid":"<sender_uuid>"}
S -> C1: FILE_TRANS_DONE_RESP {"status":"OK"}
```

The server forwards this to the receiver after changing the UUID to that of the receiver so it knows that the transfer is done:

```  
S -> C2: FILE_TRANS_DONE {"uuid":"<receiver_uuid>"}
```

Both file transfer sockets are then closed by the server.

### 10.4.2 Unhappy flow

```  
C1 -> S: FILE_TRANS_DONE {"uuid":"<sender_uuid>"}
S -> C1: FILE_TRANS_DONE_RESP {"status":"ERROR", "code":<error code>}
```

Possible `<error code>`:

| Error code | Description                                               |  
|------------|-----------------------------------------------------------|  
| 6000       | You are not logged in                                     |
| 11006      | There is no ongoing file transfer with the specified uuid |

## 10.5 File transfer cancel

If any of the two users logs out during the file transfer, or the file transfer socket is closed unexpectedly, the server sends a FILE_TRANS_CANCEL packet to both users (if only the file transfer socket was closed) or the remaining user (if a user logged out).

```  
S -> C1: FILE_TRANS_CANCEL {"uuid":"<sender_uuid>"}
S -> C2: FILE_TRANS_CANCEL {"uuid":"<receiver_uuid>"}
```

The remaining file transfer socket(s) is/are then closed.