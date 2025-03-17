package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class CodecAssertions {

    public static <T> T assertOk(@NotNull Result<T> result) {
        //noinspection unchecked
        return (T) assertInstanceOf(Result.Ok.class, result).value();
    }

    public static void assertError(@NotNull String expected, @NotNull Result<?> result) {
        final String message = assertInstanceOf(Result.Error.class, result).message();
        assertEquals(expected, message);
    }
}
