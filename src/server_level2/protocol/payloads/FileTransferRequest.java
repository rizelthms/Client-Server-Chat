package server_level2.protocol.payloads;

public record FileTransferRequest(String username, String filename, long fileSize, String checksum) {
}
