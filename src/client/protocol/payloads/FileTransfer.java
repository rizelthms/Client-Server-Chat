package client.protocol.payloads;

public record FileTransfer(String username, String filename, long fileSize, String checksum, String uuid) {
}
