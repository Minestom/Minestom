package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.Experimental
public interface Decoder<T extends @UnknownNullability Object> {

    static <T> Decoder<T> unit(T value) {
        return new Decoder<>() {
            @Override
            public <D> Result<T> decode(Transcoder<D> coder, @Nullable D ignored) {
                return new Result.Ok<>(value);
            }
        };
    }

    <D> Result<T> decode(Transcoder<D> coder, @Nullable D value);
}
