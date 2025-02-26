package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import protocoltests.extensions.ServerTestBase;
import protocoltests.protocol.messages.Enter;
import protocoltests.protocol.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvalidMessageTests extends ServerTestBase {

    @Test
    void tc71InvalidMessageHeaderShouldReturnUnknownCommand() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // enter resp

        outUser1.println("YEE invalid message header");
        String unknownCommandString = receiveLineWithTimeout(inUser1);

        assertEquals("UNKNOWN_COMMAND", unknownCommandString);
    }

    @Test
    void tc72InvalidJsonShouldReturnParseError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // enter resp

        outUser1.println("BROADCAST_REQ {\"aaaa}");
        String parseErrorString = receiveLineWithTimeout(inUser1);

        assertEquals("PARSE_ERROR", parseErrorString);
    }
}