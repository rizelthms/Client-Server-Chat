package client.cli;

import java.nio.file.Path;
import java.util.UUID;

public record OutgoingFileTransfer(String receiverUsername, Path filePath, String checksum, UUID uuid) {
}
