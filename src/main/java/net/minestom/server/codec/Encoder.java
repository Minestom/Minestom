package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Experimental
public interface Encoder<T> {

    static <T> Encoder<T> empty() {
        return new Encoder<>() {
            @Override
            public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
                return new Result.Ok<>(coder.createNull());
            }
        };
    }

    <D> Result<D> encode(Transcoder<D> coder, @Nullable T value);

}
