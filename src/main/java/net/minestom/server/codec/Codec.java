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
import org.jspecify.annotations.Nullable;

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
        static <D> RawValue of(Transcoder<D> coder, D value) {
            return new CodecImpl.RawValueImpl<>(coder, value);
        }

        <D> Result<D> convertTo(Transcoder<D> coder);
    }

    Codec<RawValue> RAW_VALUE = new CodecImpl.RawValueCodecImpl();

    Codec<Unit> UNIT = StructCodec.struct(Unit.INSTANCE);

    Codec<Boolean> BOOLEAN = new PrimitiveImpl<>(Transcoder::createBoolean, Transcoder::getBoolean);

    Codec<Byte> BYTE = new PrimitiveImpl<>(Transcoder::createByte, Transcoder::getByte);

    Codec<Short> SHORT = new PrimitiveImpl<>(Transcoder::createShort, Transcoder::getShort);

    Codec<Integer> INT = new PrimitiveImpl<>(Transcoder::createInt, Transcoder::getInt);

    Codec<Long> LONG = new PrimitiveImpl<>(Transcoder::createLong, Transcoder::getLong);

    Codec<Float> FLOAT = new PrimitiveImpl<>(Transcoder::createFloat, Transcoder::getFloat);

    Codec<Double> DOUBLE = new PrimitiveImpl<>(Transcoder::createDouble, Transcoder::getDouble);

    Codec<String> STRING = new PrimitiveImpl<>(Transcoder::createString, Transcoder::getString);

    Codec<Key> KEY = STRING.transform(Key::key, Key::asString);

    Codec<byte[]> BYTE_ARRAY = new PrimitiveImpl<>(Transcoder::createByteArray, Transcoder::getByteArray);

    Codec<int[]> INT_ARRAY = new PrimitiveImpl<>(Transcoder::createIntArray, Transcoder::getIntArray);

    Codec<long[]> LONG_ARRAY = new PrimitiveImpl<>(Transcoder::createLongArray, Transcoder::getLongArray);

    Codec<UUID> UUID = Codec.INT_ARRAY.transform(UUIDUtils::intArrayToUuid, UUIDUtils::uuidToIntArray);

    Codec<UUID> UUID_STRING = STRING.transform(java.util.UUID::fromString, java.util.UUID::toString);

    Codec<UUID> UUID_COERCED = UUID.orElse(UUID_STRING);

    Codec<Component> COMPONENT = ComponentCodecs.COMPONENT;

    Codec<Style> COMPONENT_STYLE = ComponentCodecs.STYLE;

    Codec<Point> BLOCK_POSITION = new CodecImpl.BlockPositionImpl();

    Codec<Point> VECTOR3D = new CodecImpl.Vector3DImpl();

    Codec<BinaryTag> NBT = RAW_VALUE.transform(
            value -> value.convertTo(Transcoder.NBT).orElseThrow(),
            value -> RawValue.of(Transcoder.NBT, value));

    Codec<CompoundBinaryTag> NBT_COMPOUND = NBT.transform(value -> {
        if (!(value instanceof CompoundBinaryTag compound))
            throw new IllegalArgumentException("Not a compound: " + value);
        return compound;
    }, compound -> compound);

    static <E extends Enum<E>> Codec<E> Enum(Class<E> enumClass) {
        return STRING.transform(
                value -> Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT));
    }

    static <T> Codec<T> Recursive(Function<Codec<T>, Codec<T>> func) {
        return new CodecImpl.RecursiveImpl<>(func).delegate;
    }

    static <T> Codec<T> ForwardRef(Supplier<Codec<T>> func) {
        return new CodecImpl.ForwardRefImpl<>(func);
    }

    static <T> StructCodec<T> RegistryTaggedUnion(
            Registry<StructCodec<? extends T>> registry,
            Function<T, StructCodec<? extends T>> serializerGetter,
            String key
    ) {
        return Codec.RegistryTaggedUnion((ignored) -> registry, serializerGetter, key);
    }

    static <T> StructCodec<T> RegistryTaggedUnion(
            Registries.Selector<StructCodec<? extends T>> registrySelector,
            Function<T, StructCodec<? extends T>> serializerGetter,
            String key
    ) {
        return new CodecImpl.RegistryTaggedUnionImpl<>(registrySelector, serializerGetter, key);
    }

    static <L, R> Codec<Either<L, R>> Either(Codec<L> leftCodec, Codec<R> rightCodec) {
        return new CodecImpl.EitherImpl<>(leftCodec, rightCodec);
    }

    default Codec<@Nullable T> optional() {
        return new CodecImpl.OptionalImpl<>(this, null);
    }

    default Codec<T> optional(T defaultValue) {
        return new CodecImpl.OptionalImpl<>(this, defaultValue);
    }

    default <S> Codec<S> transform(ThrowingFunction<T, S> to, ThrowingFunction<S, T> from) {
        return new CodecImpl.TransformImpl<>(this, to, from);
    }

    default Codec<List<T>> list(int maxSize) {
        return new CodecImpl.ListImpl<>(this, maxSize);
    }

    default Codec<List<T>> list() {
        return list(Integer.MAX_VALUE);
    }

    default Codec<List<T>> listOrSingle(int maxSize) {
        return Codec.this.list(maxSize).orElse(Codec.this.transform(
                List::of, list -> list.isEmpty() ? null : list.getFirst()));
    }

    default Codec<List<T>> listOrSingle() {
        return this.listOrSingle(Integer.MAX_VALUE);
    }

    default Codec<Set<T>> set(int maxSize) {
        return new CodecImpl.SetImpl<>(Codec.this, maxSize);
    }

    default Codec<Set<T>> set() {
        return set(Integer.MAX_VALUE);
    }

    default <V> Codec<Map<T, V>> mapValue(Codec<V> valueCodec, int maxSize) {
        return new CodecImpl.MapImpl<>(Codec.this, valueCodec, maxSize);
    }

    default <V> Codec<Map<T, V>> mapValue(Codec<V> valueCodec) {
        return mapValue(valueCodec, Integer.MAX_VALUE);
    }

    default <R, T1 extends T, TR extends R> StructCodec<R> unionType(Function<T, StructCodec<TR>> serializers, Function<R, T1> keyFunc) {
        return unionType("type", serializers, keyFunc);
    }

    default <R, T1 extends T, TR extends R> StructCodec<R> unionType(String keyField, Function<T, StructCodec<TR>> serializers, Function<R, T1> keyFunc) {
        return new CodecImpl.UnionImpl<>(keyField, this, serializers, keyFunc);
    }

    default Codec<T> orElse(Codec<T> other) {
        return new CodecImpl.OrElseImpl<>(this, other);
    }

}
