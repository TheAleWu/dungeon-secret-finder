package de.alewu.dsf.web;

public class RemoteDataUpdateResult {

    private final Type type;
    private String[] args;

    public RemoteDataUpdateResult(Type type, String... args) {
        this.type = type;
        this.args = args;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return String.format(getType().getMessage(), (Object[]) args);
    }

    public enum Type {

        SUCCESS("§aSuccessfully updated dungeon room data"),
        NOT_PROPERLY_FORMATTED("§cCould not update room data (Not properly formatted: %s)"),
        EXCEPTION_OCCURRED("§cCould not update room data (Exception occurred: %s)"),
        MISSING_MANDATORY_DATA("§cCould not update room data (Missing data: %s)");

        private final String message;

        Type(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
