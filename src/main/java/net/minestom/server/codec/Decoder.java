package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Decoder<T> {

    static @NotNull <T> Decoder<T> unit(@NotNull T value) {
        return new Decoder<>() {
            @Override
            public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D ignored) {
                return new Result.Ok<>(value);
            }
        };
    }

    <D> @NotNull Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value);

}
