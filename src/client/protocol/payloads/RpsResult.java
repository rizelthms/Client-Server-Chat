package client.protocol.payloads;

import client.protocol.Result;
import client.protocol.Choice;

public record RpsResult(Result result, Choice otherChoice) {
}
