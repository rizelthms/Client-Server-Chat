package server_level2.protocol;

public enum Choice {
    ROCK,
    PAPER,
    SCISSORS;

    public Result getResult(Choice otherChoice) {
        if (this == otherChoice) {
            return Result.TIE;
        }
        if (this == ROCK && otherChoice == SCISSORS ||
                this == PAPER && otherChoice == ROCK ||
                this == SCISSORS && otherChoice == PAPER) {
            return Result.WON;
        }
        return Result.LOST;
    }
}
