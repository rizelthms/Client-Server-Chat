package client.protocol.payloads;

public record FileTransferDecline(String username, String checksum) {
}
