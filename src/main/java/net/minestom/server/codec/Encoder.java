package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface Encoder<T> {

    static @NotNull <T> Encoder<T> empty() {
        return new Encoder<>() {
            @Override
            public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
                return new Result.Ok<>(coder.createNull());
            }
        };
    }

    <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value);

}
