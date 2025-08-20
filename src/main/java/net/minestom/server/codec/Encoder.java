package net.minestom.server.codec;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Encoders are interfaces used in {@link Codec} which purpose is to encode any value of {@link T}
 * with a transcoder.
 * <br>
 * For example:
 * <pre>{@code
 * record Name(String imTheBoss) { }
 * Encoder<Name> encoder = new Encoder<>() {
 *     @Override
 *     public <D> Result<D> encode(Transcoder<D> coder, @Nullable Name value) {
 *         if (value == null) return new Result.Error<>("null");
 *         return new Result.Ok<>(coder.createString(value.imTheBoss()));
 *     }
 * };
 * Result<BinaryTag> result = encoder.encode(Transcoder.NBT, new Name("me")); // Result.OK(StringBinaryTag("me"))
 * Result<BinaryTag> errorResult = encoder.encode(Transcoder.NBT, null); // Result.Error("null")
 * }</pre>
 * @param <T> the value type
 */
@FunctionalInterface
public interface Encoder<T extends @UnknownNullability Object> {

    /**
     * Creates an empty encoder that only encodes null
     * @return the empty encoder
     * @param <T> the encoder type
     */
    static <T> Encoder<T> empty() {
        return new Encoder<>() {
            @Override
            public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
                return new Result.Ok<>(coder.createNull());
            }
        };
    }

    /**
     * Encodes a value of {@link T} using the specific {@link Transcoder}
     * <br>
     * The {@link Result} will be of {@link Result.Ok} or {@link Result.Error} and its typed {@link D}
     * @param coder the transcoder to use
     * @param value the value to encode
     * @return the {@link Result} of the encoding with its type determined by the transcoder
     * @param <D> The resultant type
     */
    <D> Result<D> encode(Transcoder<D> coder, @Nullable T value);

}
