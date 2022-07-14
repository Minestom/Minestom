package net.minestom.server.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ArgumentResult<R> {
    default @Nullable R value() {
        return null;
    }

    static <T> Success<T> success(@NotNull T result) {
        return new ArgumentParser.SuccessResult<>(result);
    }

    static <T> IncompatibleType<T> incompatibleType() {
        return new ArgumentParser.IncompatibleTypeResult<>();
    }

    static <T> SyntaxError<T> syntaxError(String message, String input, int code) {
        return new ArgumentParser.SyntaxErrorResult<>(code, message, input);
    }

    sealed interface Success<T> extends ArgumentResult<T> permits ArgumentParser.SuccessResult {
        @NotNull T value();
    }

    sealed interface IncompatibleType<T> extends ArgumentResult<T> permits ArgumentParser.IncompatibleTypeResult {
    }

    sealed interface SyntaxError<T> extends ArgumentResult<T> permits ArgumentParser.SyntaxErrorResult {
        int code();

        String message();

        String input(); //todo cursor (error) position instead?
    }
}
