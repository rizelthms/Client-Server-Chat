package client.protocol.payloads;

import client.shared.Utils;
import client.protocol.StatusResult;

public record UUIDStatus(StatusResult status, int code, String uuid) {
    public String getErrorMessage() {
        return Utils.getErrorMessageForErrorCode(code);
    }
}
