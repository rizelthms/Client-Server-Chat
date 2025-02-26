package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import protocoltests.extensions.ServerTestBase;
import protocoltests.protocol.messages.*;
import protocoltests.protocol.utils.Utils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class PrivateMessageTests extends ServerTestBase {

    @Test
    void userReceivesPmResponseWhenMessageSentSuccessfully() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new PmRequest("user2", "Test Message")));
        String pmRespString = receiveLineWithTimeout(inUser1);
        PmResponse pmResponse = Utils.messageToObject(pmRespString);

        assertEquals(new PmResponse("OK", 0), pmResponse);
    }

    @Test
    void userReceivesErrorWhenSenderNotLoggedIn() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp

        outUser1.println(Utils.objectToMessage(new PmRequest("user2", "Test Message")));
        String pmRespString = receiveLineWithTimeout(inUser1);
        PmResponse pmResponse = Utils.messageToObject(pmRespString);

        assertEquals(new PmResponse("ERROR", 9000), pmResponse);
    }

    @Test
    void userReceivesErrorWhenReceiverNotLoggedIn() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready

        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser1.println(Utils.objectToMessage(new PmRequest("user2", "Test Message")));
        String pmRespString = receiveLineWithTimeout(inUser1);
        PmResponse pmResponse = Utils.messageToObject(pmRespString);

        assertEquals(new PmResponse("ERROR", 9001), pmResponse);
    }

    @Test
    void privateMessageShouldBeSentToOneUserOnly() throws IOException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        receiveLineWithTimeout(inUser3); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser3.println(Utils.objectToMessage(new Enter("user3")));
        receiveLineWithTimeout(inUser3); // enter resp
        receiveLineWithTimeout(inUser1); // joined
        receiveLineWithTimeout(inUser2); // joined


        outUser1.println(Utils.objectToMessage(new PmRequest("user2", "Test Message")));
        receiveLineWithTimeout(inUser1); // PM resp
        String pmString = receiveLineWithTimeout(inUser2);
        Pm pm = Utils.messageToObject(pmString);

        // Assert that user3 does not receive message
        assertThrows(AssertionFailedError.class, () -> receiveLineWithTimeout(inUser3));

        assertEquals(new Pm("user1", "Test Message"), pm);
    }
}