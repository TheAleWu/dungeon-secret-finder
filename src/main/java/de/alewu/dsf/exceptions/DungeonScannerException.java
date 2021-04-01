package de.alewu.dsf.exceptions;

public class DungeonScannerException extends DungeonSecretFinderException {

    public DungeonScannerException(String message) {
        super(message);
    }

    public DungeonScannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
