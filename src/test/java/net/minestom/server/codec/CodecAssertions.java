package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class CodecAssertions {

    public static <T> T assertOk(@NotNull Result<T> result) {
        return switch (result) {
            case Result.Ok(T value) -> value;
            case Result.Error(String message) -> throw new AssertionError("Expected Ok but got Error: " + message);
        };
    }

    public static void assertError(@NotNull String expected, @NotNull Result<?> result) {
        final String message = assertInstanceOf(Result.Error.class, result).message();
        assertEquals(expected, message);
    }
}
