package protocoltests.protocol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import protocoltests.protocol.messages.*;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();
    static {
        objToNameMapping.put(Enter.class, "ENTER");
        objToNameMapping.put(EnterResp.class, "ENTER_RESP");
        objToNameMapping.put(BroadcastReq.class, "BROADCAST_REQ");
        objToNameMapping.put(BroadcastResp.class, "BROADCAST_RESP");
        objToNameMapping.put(Broadcast.class, "BROADCAST");
        objToNameMapping.put(Joined.class, "JOINED");
        objToNameMapping.put(ParseError.class, "PARSE_ERROR");
        objToNameMapping.put(Pong.class, "PONG");
        objToNameMapping.put(PongError.class, "PONG_ERROR");
        objToNameMapping.put(Ready.class, "READY");
        objToNameMapping.put(Ping.class, "PING");
        objToNameMapping.put(Hangup.class, "HANGUP");
        objToNameMapping.put(ByeResp.class, "BYE_RESP");
        objToNameMapping.put(Left.class, "LEFT");
        objToNameMapping.put(ListUsersResponse.class, "LIST_USERS_RESP");
        objToNameMapping.put(PmRequest.class, "PM_REQ");
        objToNameMapping.put(PmResponse.class, "PM_RESP");
        objToNameMapping.put(Pm.class, "PM");
        objToNameMapping.put(RpsRequest.class, "RPS_REQ");
        objToNameMapping.put(RpsRequestResponse.class, "RPS_REQ_RESP");
        objToNameMapping.put(RpsInvite.class, "RPS_INVITE");
        objToNameMapping.put(RpsAccept.class, "RPS_ACCEPT");
        objToNameMapping.put(RpsAcceptResponse.class, "RPS_ACCEPT_RESP");
        objToNameMapping.put(RpsDecline.class, "RPS_DECLINE");
        objToNameMapping.put(RpsDeclineResponse.class, "RPS_DECLINE_RESP");
        objToNameMapping.put(RpsStart.class, "RPS_START");
        objToNameMapping.put(RpsChoice.class, "RPS_CHOICE");
        objToNameMapping.put(RpsChoiceResponse.class, "RPS_CHOICE_RESP");
        objToNameMapping.put(RpsResult.class, "RPS_RESULT");
    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);
        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message");
        }
        String body = mapper.writeValueAsString(object);
        return header + " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }

    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}