package server_level2.protocol.payloads;

import com.fasterxml.jackson.annotation.JsonIgnore;
import server_level2.protocol.Command;

public interface Payload {
    @JsonIgnore
    Command getCommand();
}
