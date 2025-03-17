package net.minestom.server.codec;

import net.minestom.server.codec.CodecImpl.PrimitiveImpl;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * <p>A {@link Codec} represents a combined {@link Encoder} and {@link Decoder} for a value.</p>
 *
 * <p>Heavily inspired by <a href="https://github.com/Mojang/DataFixerUpper">Mojang/DataFixerUpper</a>,
 * licensed under the MIT license.</p>
 *
 * @param <T> The type to be represented by this codec
 */
@ApiStatus.Experimental
public interface Codec<T> extends Encoder<T>, Decoder<T> {

    @NotNull Codec<Unit> UNIT = StructCodec.struct(() -> Unit.INSTANCE);

    @NotNull Codec<Boolean> BOOLEAN = new PrimitiveImpl<>(Transcoder::createBoolean, Transcoder::getBoolean);

    @NotNull Codec<Byte> BYTE = new PrimitiveImpl<>(Transcoder::createByte, Transcoder::getByte);

    @NotNull Codec<Short> SHORT = new PrimitiveImpl<>(Transcoder::createShort, Transcoder::getShort);

    @NotNull Codec<Integer> INT = new PrimitiveImpl<>(Transcoder::createInt, Transcoder::getInt);

    @NotNull Codec<Long> LONG = new PrimitiveImpl<>(Transcoder::createLong, Transcoder::getLong);

    @NotNull Codec<Float> FLOAT = new PrimitiveImpl<>(Transcoder::createFloat, Transcoder::getFloat);

    @NotNull Codec<Double> DOUBLE = new PrimitiveImpl<>(Transcoder::createDouble, Transcoder::getDouble);

    @NotNull Codec<String> STRING = new PrimitiveImpl<>(Transcoder::createString, Transcoder::getString);

    @NotNull Codec<byte[]> BYTE_ARRAY = new PrimitiveImpl<>(Transcoder::createByteArray, Transcoder::getByteArray);

    @NotNull Codec<int[]> INT_ARRAY = new PrimitiveImpl<>(Transcoder::createIntArray, Transcoder::getIntArray);

    @NotNull Codec<long[]> LONG_ARRAY = new PrimitiveImpl<>(Transcoder::createLongArray, Transcoder::getLongArray);

    default @NotNull Codec<@Nullable T> optional() {
        return new CodecImpl.OptionalImpl<>(this, null);
    }

    default @NotNull Codec<T> optional(@NotNull T defaultValue) {
        return new CodecImpl.OptionalImpl<>(this, defaultValue);
    }

    default <S> @NotNull Codec<S> transform(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
        return new CodecImpl.TransformImpl<>(this, to, from);
    }

    default @NotNull Codec<List<T>> list(int maxSize) {
        return new CodecImpl.ListImpl<>(this, maxSize);
    }

    default @NotNull Codec<List<T>> list() {
        return list(Integer.MAX_VALUE);
    }

}
