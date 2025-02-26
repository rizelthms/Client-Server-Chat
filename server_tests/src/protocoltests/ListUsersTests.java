package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import protocoltests.extensions.ServerTestBase;
import protocoltests.protocol.messages.Enter;
import protocoltests.protocol.messages.ListUsersResponse;
import protocoltests.protocol.messages.User;
import protocoltests.protocol.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUsersTests extends ServerTestBase {

    @Test
    void listUsersShouldReturnAllUsers() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready
        receiveLineWithTimeout(inUser2); // ready
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1); // enter resp

        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        receiveLineWithTimeout(inUser1); // joined
        receiveLineWithTimeout(inUser2); // enter resp

        outUser1.println("LIST_USERS_REQ");
        String listUsersString = receiveLineWithTimeout(inUser1);
        ListUsersResponse listUsersResponse = Utils.messageToObject(listUsersString);

        assertArrayEquals(new User[]{new User("user1"), new User("user2")}, listUsersResponse.users());
    }

    @Test
    void listUsersShouldReturnErrorWhenUserIsNotLoggedIn() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready

        outUser1.println("LIST_USERS_REQ");
        String listUsersString = receiveLineWithTimeout(inUser1);
        ListUsersResponse listUsersResponse = Utils.messageToObject(listUsersString);

        assertEquals(new ListUsersResponse(null, "ERROR", 6000), listUsersResponse);
    }
}