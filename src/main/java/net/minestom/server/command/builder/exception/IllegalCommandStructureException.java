package net.minestom.server.command.builder.exception;

public class IllegalCommandStructureException extends RuntimeException {
    public IllegalCommandStructureException() {
    }

    public IllegalCommandStructureException(String message) {
        super(message);
    }
}
