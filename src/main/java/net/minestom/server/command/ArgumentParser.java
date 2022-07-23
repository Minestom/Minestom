package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

class ArgumentParser {

    private ArgumentParser() {
        //no instance
    }

    public static <T> ArgumentResult<T> parse(Argument<T> argument, CommandStringReader reader) {
        // Handle specific type without loop
        try {
            // Single word argument
            if (!argument.allowSpace()) return ArgumentResult.success(argument.parse(reader.readWord()));
            // Complete input argument
            if (argument.useRemaining()) return ArgumentResult.success(argument.parse(reader.readRemaining()));
        } catch (ArgumentSyntaxException ignored) {
            return ArgumentResult.incompatibleType();
        }
        // Bruteforce
        StringBuilder current = new StringBuilder(reader.readWord());
        while (true) {
            try {
                return ArgumentResult.success(argument.parse(current.toString()));
            } catch (ArgumentSyntaxException ignored) {
                if (!reader.hasRemaining()) break;
                current.append(" ");
                current.append(reader.readWord());
            }
        }
        return ArgumentResult.incompatibleType();
    }

    record SuccessResult<R>(R value) implements ArgumentResult.Success<R> {
    }

    record IncompatibleTypeResult<R>() implements ArgumentResult.IncompatibleType<R> {
    }

    record SyntaxErrorResult<R>(int code, String message, String input) implements ArgumentResult.SyntaxError<R> {
    }
}
