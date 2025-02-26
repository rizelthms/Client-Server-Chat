package protocoltests.protocol.messages;

public record ListUsersResponse(User[] users, String status, int code) {
}