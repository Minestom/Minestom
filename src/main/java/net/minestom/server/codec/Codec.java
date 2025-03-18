package net.minestom.server.codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.codec.CodecImpl.PrimitiveImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    @NotNull Codec<Key> KEY = STRING.transform(Key::key, Key::asString);

    @NotNull Codec<byte[]> BYTE_ARRAY = new PrimitiveImpl<>(Transcoder::createByteArray, Transcoder::getByteArray);

    @NotNull Codec<int[]> INT_ARRAY = new PrimitiveImpl<>(Transcoder::createIntArray, Transcoder::getIntArray);

    @NotNull Codec<long[]> LONG_ARRAY = new PrimitiveImpl<>(Transcoder::createLongArray, Transcoder::getLongArray);

    @NotNull Codec<UUID> UUID = null; // TODO(1.21.5)

    @NotNull Codec<Component> COMPONENT = null; // TODO(1.21.5)

    @NotNull Codec<Style> COMPONENT_STYLE = null; // TODO(1.21.5)

    @NotNull Codec<Point> BLOCK_POSITION = new CodecImpl.BlockPositionImpl();

    static <E extends Enum<E>> @NotNull Codec<E> Enum(@NotNull Class<E> enumClass) {
        // TODO: this needs to handle exceptions better, and support non-enum named things.
        return STRING.transform(
                value -> Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    static <T> @NotNull Codec<T> Recursive(@NotNull Function<Codec<T>, Codec<T>> func) {
        return new CodecImpl.RecursiveImpl<>(func);
    }

    static <T> @NotNull Codec<DynamicRegistry.Key<T>> RegistryKey(@NotNull Registries.Selector<T> selector) {
        throw new UnsupportedOperationException("todo"); // TODO(1.21.5)
    }

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

    default @NotNull Codec<List<T>> listOrSingle(int maxSize) {
        throw new UnsupportedOperationException("todo"); // TODO(1.21.5)
    }

    default @NotNull Codec<Set<T>> set(int maxSize) {
        throw new UnsupportedOperationException("todo"); // TODO(1.21.5)
    }

    default @NotNull Codec<Set<T>> set() {
        return set(Integer.MAX_VALUE);
    }

    default <V> @NotNull Codec<Map<T, V>> mapValue(@NotNull Codec<V> valueCodec, int maxSize) {
        throw new UnsupportedOperationException("todo"); // TODO(1.21.5)
    }

    default <V> @NotNull Codec<Map<T, V>> mapValue(@NotNull Codec<V> valueCodec) {
        return mapValue(valueCodec, Integer.MAX_VALUE);
    }

    default <R> Codec<R> unionType(@NotNull Function<T, Codec<R>> serializers, @NotNull Function<R, T> keyFunc) {
        return unionType("type", serializers, keyFunc);
    }

    default <R> Codec<R> unionType(@NotNull String keyField, @NotNull Function<T, Codec<R>> serializers, @NotNull Function<R, T> keyFunc) {
        return new CodecImpl.UnionImpl<>(keyField, Codec.this, serializers, keyFunc);
    }

    default Codec<T> orElse(@NotNull Codec<T> other) {
        return new CodecImpl.OrElseImpl<>(this, other);
    }

}
