package net.minestom.server.codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.codec.CodecImpl.PrimitiveImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.ThrowingFunction;
import net.minestom.server.utils.UUIDUtils;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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

    sealed interface RawValue permits CodecImpl.RawValueImpl {
        static <D> @NotNull RawValue of(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new CodecImpl.RawValueImpl<>(coder, value);
        }

        <D> @NotNull Result<D> convertTo(@NotNull Transcoder<D> coder);
    }

    @NotNull Codec<RawValue> RAW_VALUE = new CodecImpl.RawValueCodecImpl();

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

    @NotNull Codec<UUID> UUID = Codec.INT_ARRAY.transform(UUIDUtils::intArrayToUuid, UUIDUtils::uuidToIntArray);

    @NotNull Codec<UUID> UUID_COERCED = UUID.orElse(Codec.STRING.transform(java.util.UUID::fromString, java.util.UUID::toString));

    @NotNull Codec<Component> COMPONENT = ComponentCodecs.COMPONENT;
    
    @NotNull Codec<Style> COMPONENT_STYLE = ComponentCodecs.STYLE;

    @NotNull Codec<Point> BLOCK_POSITION = new CodecImpl.BlockPositionImpl();

    @NotNull Codec<Point> VECTOR3D = new CodecImpl.Vector3DImpl();

    @NotNull Codec<BinaryTag> NBT = RAW_VALUE.transform(
            value -> value.convertTo(Transcoder.NBT).orElseThrow(),
            value -> RawValue.of(Transcoder.NBT, value));

    @NotNull Codec<CompoundBinaryTag> NBT_COMPOUND = NBT.transform(value -> {
        if (!(value instanceof CompoundBinaryTag compound))
            throw new IllegalArgumentException("Not a compound: " + value);
        return compound;
    }, compound -> compound);

    static <E extends Enum<E>> @NotNull Codec<E> Enum(@NotNull Class<E> enumClass) {
        return STRING.transform(
                value -> Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT));
    }

    static <T> @NotNull Codec<T> Recursive(@NotNull Function<Codec<T>, Codec<T>> func) {
        return new CodecImpl.RecursiveImpl<>(func).delegate;
    }

    static <T> @NotNull Codec<T> ForwardRef(@NotNull Supplier<Codec<T>> func) {
        return new CodecImpl.ForwardRefImpl<>(func);
    }

    static <T> @NotNull StructCodec<T> RegistryTaggedUnion(
            @NotNull Registry<StructCodec<? extends T>> registry,
            @NotNull Function<T, StructCodec<? extends T>> serializerGetter,
            @NotNull String key
    ) {
        return Codec.RegistryTaggedUnion((ignored) -> registry, serializerGetter, key);
    }

    static <T> @NotNull StructCodec<T> RegistryTaggedUnion(
            @NotNull Registries.Selector<StructCodec<? extends T>> registrySelector,
            @NotNull Function<T, StructCodec<? extends T>> serializerGetter,
            @NotNull String key
    ) {
        return new CodecImpl.RegistryTaggedUnionImpl<>(registrySelector, serializerGetter, key);
    }

    static <L, R> @NotNull Codec<Either<L, R>> Either(@NotNull Codec<L> leftCodec, @NotNull Codec<R> rightCodec) {
        return new CodecImpl.EitherImpl<>(leftCodec, rightCodec);
    }

    default @NotNull Codec<@Nullable T> optional() {
        return new CodecImpl.OptionalImpl<>(this, null);
    }

    default @NotNull Codec<T> optional(@NotNull T defaultValue) {
        return new CodecImpl.OptionalImpl<>(this, defaultValue);
    }

    default <S> @NotNull Codec<S> transform(@NotNull ThrowingFunction<T, S> to, @NotNull ThrowingFunction<S, T> from) {
        return new CodecImpl.TransformImpl<>(this, to, from);
    }

    default @NotNull Codec<List<T>> list(int maxSize) {
        return new CodecImpl.ListImpl<>(this, maxSize);
    }

    default @NotNull Codec<List<T>> list() {
        return list(Integer.MAX_VALUE);
    }

    default @NotNull Codec<List<T>> listOrSingle(int maxSize) {
        return Codec.this.list(maxSize).orElse(Codec.this.transform(
                List::of, list -> list.isEmpty() ? null : list.getFirst()));
    }

    default @NotNull Codec<List<T>> listOrSingle() {
        return this.listOrSingle(Integer.MAX_VALUE);
    }

    default @NotNull Codec<Set<T>> set(int maxSize) {
        return new CodecImpl.SetImpl<>(Codec.this, maxSize);
    }

    default @NotNull Codec<Set<T>> set() {
        return set(Integer.MAX_VALUE);
    }

    default <V> @NotNull Codec<Map<T, V>> mapValue(@NotNull Codec<V> valueCodec, int maxSize) {
        return new CodecImpl.MapImpl<>(Codec.this, valueCodec, maxSize);
    }

    default <V> @NotNull Codec<Map<T, V>> mapValue(@NotNull Codec<V> valueCodec) {
        return mapValue(valueCodec, Integer.MAX_VALUE);
    }

    default <R, T1 extends T, TR extends R> StructCodec<R> unionType(@NotNull Function<T, StructCodec<TR>> serializers, @NotNull Function<R, T1> keyFunc) {
        return unionType("type", serializers, keyFunc);
    }

    default <R, T1 extends T, TR extends R> StructCodec<R> unionType(@NotNull String keyField, @NotNull Function<T, StructCodec<TR>> serializers, @NotNull Function<R, T1> keyFunc) {
        return new CodecImpl.UnionImpl<>(keyField, this, serializers, keyFunc);
    }

    default Codec<T> orElse(@NotNull Codec<T> other) {
        return new CodecImpl.OrElseImpl<>(this, other);
    }

}
