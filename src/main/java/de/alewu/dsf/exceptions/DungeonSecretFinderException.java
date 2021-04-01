package de.alewu.dsf.exceptions;

public class DungeonSecretFinderException extends RuntimeException {

    private String clientMessage;

    public DungeonSecretFinderException(String message) {
        super(message);
    }

    public DungeonSecretFinderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DungeonSecretFinderException withClientMessage(String message) {
        this.clientMessage = message;
        return this;
    }

    public String getClientMessage() {
        return clientMessage;
    }
}
