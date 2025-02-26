package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import protocoltests.extensions.ServerTestBase;
import protocoltests.protocol.messages.*;
import protocoltests.protocol.utils.Utils;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;

public class RpsTests extends ServerTestBase {

    @Test
    void rpsRequestWhenNothingGoesWrongShouldReturnStatusOk() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        String rpsRespString = receiveLineWithTimeout(inUser1);
        RpsRequestResponse rpsReqResponse = Utils.messageToObject(rpsRespString);

        assertEquals(new RpsRequestResponse("OK", 0), rpsReqResponse);
    }

    @Test
    void rpsRequestWhenSenderIsNotLoggedInShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        String rpsRespString = receiveLineWithTimeout(inUser1);
        RpsRequestResponse rpsReqResponse = Utils.messageToObject(rpsRespString);

        assertEquals(new RpsRequestResponse("ERROR", 6000), rpsReqResponse);
    }

    @Test
    void rpsRequestWhenReceiverIsNotLoggedInShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        String rpsRespString = receiveLineWithTimeout(inUser1);
        RpsRequestResponse rpsReqResponse = Utils.messageToObject(rpsRespString);

        assertEquals(new RpsRequestResponse("ERROR", 10001), rpsReqResponse);
    }

    @Test
    void rpsRequestWhenInvitedUserIsInGameShouldReturnError() throws JsonProcessingException {
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

        outUser2.println(Utils.objectToMessage(new RpsRequest("user3")));
        receiveLineWithTimeout(inUser2); // rps req resp
        receiveLineWithTimeout(inUser3); // rps invite
        outUser3.println(Utils.objectToMessage(new RpsAccept("user2")));
        receiveLineWithTimeout(inUser3); // rps accept resp
        receiveLineWithTimeout(inUser3); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        String rpsRespString = receiveLineWithTimeout(inUser1);
        RpsRequestResponse rpsReqResponse = Utils.messageToObject(rpsRespString);

        assertEquals(new RpsRequestResponse("ERROR", 10002), rpsReqResponse);
    }

    @Test
    void rpsRequestWhenInvitingUserIsInGameShouldReturnError() throws JsonProcessingException {
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

        outUser1.println(Utils.objectToMessage(new RpsRequest("user3")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser3); // rps invite
        outUser3.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser3); // rps accept resp
        receiveLineWithTimeout(inUser3); // rps start
        receiveLineWithTimeout(inUser1); // rps start

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        String rpsRespString = receiveLineWithTimeout(inUser1);
        RpsRequestResponse rpsReqResponse = Utils.messageToObject(rpsRespString);

        assertEquals(new RpsRequestResponse("ERROR", 10003), rpsReqResponse);
    }

    @Test
    void rpsRequestWhenUserInvitesThemselvesShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser1.println(Utils.objectToMessage(new RpsRequest("user1")));
        String rpsRespString = receiveLineWithTimeout(inUser1);
        RpsRequestResponse rpsReqResponse = Utils.messageToObject(rpsRespString);

        assertEquals(new RpsRequestResponse("ERROR", 10004), rpsReqResponse);
    }

    @Test
    void rpsRequestShouldSendInviteToOtherPlayer() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        String inviteString = receiveLineWithTimeout(inUser2);
        RpsInvite invite = Utils.messageToObject(inviteString);

        assertEquals(new RpsInvite("user1"), invite);
    }

    @Test
    void rpsAcceptShouldReturnOk() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        String rpsAcceptResponseString = receiveLineWithTimeout(inUser2);
        RpsAcceptResponse response = Utils.messageToObject(rpsAcceptResponseString);

        assertEquals(new RpsAcceptResponse("OK", 0), response);
    }

    @Test
    void rpsAcceptWhenUserIsNotLoggedInShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready

        outUser1.println(Utils.objectToMessage(new RpsAccept("asd")));
        String rpsAcceptResponseString = receiveLineWithTimeout(inUser1);
        RpsAcceptResponse response = Utils.messageToObject(rpsAcceptResponseString);

        assertEquals(new RpsAcceptResponse("ERROR", 6000), response);
    }

    @Test
    void rpsAcceptWhenNoInviteShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        String rpsAcceptResponseString = receiveLineWithTimeout(inUser2);
        RpsAcceptResponse response = Utils.messageToObject(rpsAcceptResponseString);

        assertEquals(new RpsAcceptResponse("ERROR", 10006), response);
    }

    @Test
    void rpsAcceptWhenInvitingUserLoggedOutShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite

        outUser1.println("BYE");
        receiveLineWithTimeout(inUser1); // bye resp
        receiveLineWithTimeout(inUser2); // left

        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        String rpsAcceptResponseString = receiveLineWithTimeout(inUser2);
        RpsAcceptResponse response = Utils.messageToObject(rpsAcceptResponseString);

        assertEquals(new RpsAcceptResponse("ERROR", 10007), response);
    }

    @Test
    void rpsAcceptWhenInvitingUserStartedGameWithOtherPlayerShouldReturnError() throws JsonProcessingException {
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

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite

        outUser1.println(Utils.objectToMessage(new RpsRequest("user3")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser3); // rps invite

        outUser3.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser3); // rps accept resp
        receiveLineWithTimeout(inUser3); // rps start
        receiveLineWithTimeout(inUser1); // rps start

        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        String rpsAcceptResponseString = receiveLineWithTimeout(inUser2);
        RpsAcceptResponse response = Utils.messageToObject(rpsAcceptResponseString);

        assertEquals(new RpsAcceptResponse("ERROR", 10008), response);
    }

    @Test
    void rpsAcceptWhenInvitedUserStartedGameWithOtherPlayerShouldReturnError() throws JsonProcessingException {
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

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite

        outUser2.println(Utils.objectToMessage(new RpsRequest("user3")));
        receiveLineWithTimeout(inUser2); // rps req resp
        receiveLineWithTimeout(inUser3); // rps invite

        outUser3.println(Utils.objectToMessage(new RpsAccept("user2")));
        receiveLineWithTimeout(inUser3); // rps accept resp
        receiveLineWithTimeout(inUser3); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        String rpsAcceptResponseString = receiveLineWithTimeout(inUser2);
        RpsAcceptResponse response = Utils.messageToObject(rpsAcceptResponseString);

        assertEquals(new RpsAcceptResponse("ERROR", 10003), response);
    }

    @Test
    void rpsDeclineWhenUserIsNotLoggedInShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready

        outUser1.println(Utils.objectToMessage(new RpsDecline("asd")));
        String rpsDeclineResponseString = receiveLineWithTimeout(inUser1);
        RpsDeclineResponse response = Utils.messageToObject(rpsDeclineResponseString);

        assertEquals(new RpsDeclineResponse("ERROR", 6000), response);
    }

    @Test
    void rpsDeclineWhenNoInviteShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser2.println(Utils.objectToMessage(new RpsDecline("user1")));
        String rpsDeclineResponseString = receiveLineWithTimeout(inUser2);
        RpsDeclineResponse response = Utils.messageToObject(rpsDeclineResponseString);

        assertEquals(new RpsDeclineResponse("ERROR", 10011), response);
    }

    @Test
    void rpsAcceptShouldStartGame() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser2); // rps accept resp

        String rpsStartString1 = receiveLineWithTimeout(inUser1);
        String rpsStartString2 = receiveLineWithTimeout(inUser2);
        RpsStart rpsStart1 = Utils.messageToObject(rpsStartString1);
        RpsStart rpsStart2 = Utils.messageToObject(rpsStartString2);

        assertEquals(new RpsStart("user2"), rpsStart1);
        assertEquals(new RpsStart("user1"), rpsStart2);
    }

    @Test
    void rpsDeclineShouldSendDeclineToInvitingPlayer() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsDecline("user1")));
        receiveLineWithTimeout(inUser2); // rps decline resp

        String rpsDeclineString = receiveLineWithTimeout(inUser1);
        RpsDecline rpsDecline = Utils.messageToObject(rpsDeclineString);

        assertEquals(new RpsDecline("user2"), rpsDecline);
    }

    @Test
    void rpsChoiceShouldReturnOk() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser2); // rps accept resp

        receiveLineWithTimeout(inUser1); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        outUser1.println(Utils.objectToMessage(new RpsChoice("ROCK")));
        String rpsChoiceRespString = receiveLineWithTimeout(inUser1);
        RpsChoiceResponse response = Utils.messageToObject(rpsChoiceRespString);

        assertEquals(new RpsChoiceResponse("OK", 0), response);
    }

    @Test
    void rpsChoiceWhenUserNotLoggedInShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready

        outUser1.println(Utils.objectToMessage(new RpsChoice("ROCK")));
        String rpsChoiceRespString = receiveLineWithTimeout(inUser1);
        RpsChoiceResponse response = Utils.messageToObject(rpsChoiceRespString);

        assertEquals(new RpsChoiceResponse("ERROR", 6000), response);
    }

    @Test
    void rpsChoiceWhenNoGameOngoingShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsChoice("ROCK")));
        String rpsChoiceRespString = receiveLineWithTimeout(inUser1);
        RpsChoiceResponse response = Utils.messageToObject(rpsChoiceRespString);

        assertEquals(new RpsChoiceResponse("ERROR", 10013), response);
    }

    @Test
    void rpsChoiceWhenChoiceIsInvalidShouldReturnError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser2); // rps accept resp

        receiveLineWithTimeout(inUser1); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        outUser1.println(Utils.objectToMessage(new RpsChoice("NotAValidChoice")));
        String rpsChoiceRespString = receiveLineWithTimeout(inUser1);
        RpsChoiceResponse response = Utils.messageToObject(rpsChoiceRespString);

        assertEquals(new RpsChoiceResponse("ERROR", 10014), response);
    }

    @Test
    void rpsWhenNoUserChoiceIn60SecondsShouldReturnTimeout() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser2); // rps accept resp

        receiveLineWithTimeout(inUser1); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        String timeoutString1 = assertTimeoutPreemptively(ofSeconds(60), () -> {
            String value;
            while ((value = inUser1.readLine()).equals("PING")) {
                outUser1.println("PONG");
            }
            // return the first value that is not a ping
            return value;
        });

        assertEquals("RPS_TIMEOUT", timeoutString1);
    }

    @Test
    void rpsWhenBothPlayersMakeDifferentChoiceShouldReturnCorrectResults() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser2); // rps accept resp

        receiveLineWithTimeout(inUser1); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        outUser1.println(Utils.objectToMessage(new RpsChoice("ROCK")));
        outUser2.println(Utils.objectToMessage(new RpsChoice("PAPER")));

        receiveLineWithTimeout(inUser1); // rps choice resp
        receiveLineWithTimeout(inUser2); // rps choice resp

        String rpsResultString1 = receiveLineWithTimeout(inUser1);
        String rpsResultString2 = receiveLineWithTimeout(inUser2);

        RpsResult result1 = Utils.messageToObject(rpsResultString1);
        RpsResult result2 = Utils.messageToObject(rpsResultString2);

        assertEquals(new RpsResult("LOST", "PAPER"), result1);
        assertEquals(new RpsResult("WON", "ROCK"), result2);
    }

    @Test
    void rpsWhenBothPlayersMakeSameChoiceShouldReturnTieResults() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser2); // enter resp
        receiveLineWithTimeout(inUser1); // joined

        outUser1.println(Utils.objectToMessage(new RpsRequest("user2")));
        receiveLineWithTimeout(inUser1); // rps req resp
        receiveLineWithTimeout(inUser2); // rps invite
        outUser2.println(Utils.objectToMessage(new RpsAccept("user1")));
        receiveLineWithTimeout(inUser2); // rps accept resp

        receiveLineWithTimeout(inUser1); // rps start
        receiveLineWithTimeout(inUser2); // rps start

        outUser1.println(Utils.objectToMessage(new RpsChoice("ROCK")));
        outUser2.println(Utils.objectToMessage(new RpsChoice("ROCK")));

        receiveLineWithTimeout(inUser1); // rps choice resp
        receiveLineWithTimeout(inUser2); // rps choice resp

        String rpsResultString1 = receiveLineWithTimeout(inUser1);
        String rpsResultString2 = receiveLineWithTimeout(inUser2);

        RpsResult result1 = Utils.messageToObject(rpsResultString1);
        RpsResult result2 = Utils.messageToObject(rpsResultString2);

        assertEquals(new RpsResult("TIE", "ROCK"), result1);
        assertEquals(new RpsResult("TIE", "ROCK"), result2);
    }
}
