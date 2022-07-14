package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.Argument;

class ArgumentParser {

    private ArgumentParser() {
        //no instance
    }

    public static <T> ArgumentResult<T> parse(Argument<T> argument, CommandStringReader reader) {
        try {
            if (argument.useRemaining()) return ArgumentResult.success(argument.parse(reader.readRemaining()));
            if (!argument.allowSpace()) return ArgumentResult.success(argument.parse(reader.readWord()));
            // Bruteforce
            StringBuilder current = new StringBuilder(reader.readWord());
            do {
                try {
                    return ArgumentResult.success(argument.parse(current.toString()));
                } catch (Exception ignored) {
                    current.append(" ");
                    current.append(reader.readWord());
                }
            } while (reader.hasRemaining());
        } catch (Exception ignored) {
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
