package client.protocol.payloads;

import client.shared.Utils;
import client.protocol.StatusResult;

public record Status(StatusResult status, int code) {
    public String getErrorMessage() {
        return Utils.getErrorMessageForErrorCode(code);
    }
}
