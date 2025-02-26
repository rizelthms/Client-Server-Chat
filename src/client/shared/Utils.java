package client.shared;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Utils {

    public static final int FILE_TRANSFER_PORT = 1338;

    public static String getFileHash(File file) throws NoSuchAlgorithmException, IOException {
        byte[] buffer = new byte[8192];
        int count;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        while ((count = bis.read(buffer)) > 0) {
            digest.update(buffer, 0, count);
        }
        byte[] hash = digest.digest();

        final StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getErrorMessageForErrorCode(int code) {
        return switch (code) {
            case 0 -> "";
            case 5000 -> "Login failed: A user with this name already exists. Please try a different username.";
            case 5001 -> "Login failed: The username has an invalid format or length. Please try a valid username (3-14 characters, only letters, numbers, and underscores).";
            case 5002 -> "Login failed: You are already logged in.";
            case 6000 -> "You are not logged in";
            case 9000 -> "You can't send a private message while you're not logged in";
            case 9001 -> "The user you want to send a message to is not logged in";
            case 10001 -> "The user you challenged is not logged in";
            case 10002 -> "The user that is challenged is already in a game with someone else";
            case 10003 -> "You are already in a game with someone else";
            case 10004 -> "You tried to invite yourself to a game";
            case 10006 -> "The user whose invite you accepted did not invite you";
            case 10007 -> "The user whose invite you accepted is no longer logged in";
            case 10008 -> "The user whose invite you accepted has since started a game with someone else";
            case 10011 -> "The user whose invite you declined did not invite you";
            case 10013 -> "No game is ongoing";
            case 10014 -> "Invalid choice (not \"ROCK\", \"PAPER\" or \"SCISSORS\")";
            case 11000 -> "The file receiver is not logged in";
            case 11001 -> "File size must be bigger than 0";
            case 11002 -> "Invalid file hash. Sha256 hash required";
            case 11003 -> "You can't send a file to yourself";
            case 11004 -> "The specified user did not request to send a file with the specified checksum";
            case 11005 -> "The sending user is no longer logged in";
            case 11006 -> "There is no ongoing file transfer with the specified uuid";
            default -> "Unknown Error";
        };
    }

    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }
}
