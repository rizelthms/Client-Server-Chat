package client.protocol.payloads;

import client.cli.User;

public record ListUsersResponse(User[] users) {
}
