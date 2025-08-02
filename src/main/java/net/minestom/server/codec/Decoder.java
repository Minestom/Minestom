package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface Decoder<T> {

    static <T> Decoder<T> unit(T value) {
        return new Decoder<>() {
            @Override
            public <D> Result<T> decode(Transcoder<D> coder, D ignored) {
                return new Result.Ok<>(value);
            }
        };
    }

    <D> Result<T> decode(Transcoder<D> coder, D value);

}
