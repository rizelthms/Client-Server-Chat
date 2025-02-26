package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import protocoltests.extensions.ServerTestBase;
import protocoltests.protocol.messages.ByeResp;
import protocoltests.protocol.messages.Enter;
import protocoltests.protocol.messages.Left;
import protocoltests.protocol.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TerminationTests extends ServerTestBase {

    @Test
    void serverSendsByeRespWhenClientSendsBye() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        outUser1.println(Utils.objectToMessage(new Enter("first")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser1.println("BYE");
        String byeRespString = receiveLineWithTimeout(inUser1);
        ByeResp byeResp = Utils.messageToObject(byeRespString);

        assertEquals(new ByeResp("OK"), byeResp);
    }

    @Test
    void byeShouldSendLeftToOtherUsers() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp

        outUser1.println("BYE");
        String leftString = receiveLineWithTimeout(inUser2);
        Left left = Utils.messageToObject(leftString);

        assertEquals(new Left("user1"), left);
    }
}