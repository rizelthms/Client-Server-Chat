package client.protocol.payloads;

public record FileTransferAccept(String username, String checksum, String uuid) {
}
