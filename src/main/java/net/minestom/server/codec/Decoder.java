package net.minestom.server.codec;

import org.jetbrains.annotations.UnknownNullability;


/**
 * Decoders are interfaces used in {@link Codec} which purpose is to decode any value of {@link T}
 * with a transcoder.
 * <br>
 * For example:
 * <pre>{@code
 * record Name(String imTheBoss) { }
 * Decoder<Name> decoder = new Decoder<>() {
 *     @Override
 *     public <D> Result<Name> decode(Transcoder<D> coder, D value) {
 *         return coder.getString(value).mapResult(Name::new);
 *     }
 * };
 * Result<Name> result = decoder.decode(Transcoder.NBT, StringBinaryTag.stringBinaryTag("me")); // Result.OK(Name("me"))
 * Result<Name> errorResult = decoder.decode(Transcoder.NBT, EndBinaryTag.endBinaryTag()); // Result.Error(...)
 * }</pre>
 * @param <T> the value type
 */
@FunctionalInterface
public interface Decoder<T extends @UnknownNullability Object> {

    /**
     * Returns a unit decoder of T
     * @param value the value to always return
     * @return the unit decoder
     * @param <T> the type of value
     */
    static <T> Decoder<T> unit(T value) {
        return new Decoder<>() {
            @Override
            public <D> Result<T> decode(Transcoder<D> coder, D ignored) {
                return new Result.Ok<>(value);
            }
        };
    }

    /**
     * Decodes a value of {@link D} using the specific {@link Transcoder}
     * <br>
     * The {@link Result} will be of {@link Result.Ok} or {@link Result.Error} and its typed {@link T}
     * @param coder the transcoder to use
     * @param value the value to decode
     * @return the {@link Result} of the encoding with its type determined by the transcoder
     * @param <D> The value type
     */
    <D> Result<T> decode(Transcoder<D> coder, D value);

}
