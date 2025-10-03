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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * A {@link Codec} represents a combined {@link Encoder} and {@link Decoder} for a value.
 * Enabling easy encoding and decoding of values to and from a between formats, making serialization simple, reusable and type safe.
 * Going between formats is handled by {@link Transcoder}.
 * </p>
 * <p>
 * Most of the primitive or commonly used codecs are provided as static fields in this interface.
 * For example, {@link Codec#INT} is a codec for integers, and {@link Codec#STRING} is a codec for strings.
 * You can even use {@link Codec#Enum(Class)} for enums, which will convert the enum to a string
 * representation and back.
 * </p>
 * Codecs are immutable, you must chain methods to create a codec that you want. For example
 * <pre>{@code
 *         Codec<@Nullable String> codec = Codec.STRING.optional()
 *         Codec<Set<@Nullable String>> setCodec = codec.set();
 *     }
 * </pre>
 * <p>
 * Heavily inspired by <a href="https://github.com/Mojang/DataFixerUpper">Mojang/DataFixerUpper</a>,
 * licensed under the MIT license.
 * </p>
 *
 * @param <T> The type to be represented by this codec, nullable T will provide nullable results.
 */
public interface Codec<T extends @UnknownNullability Object> extends Encoder<T>, Decoder<T> {

    /**
     * A raw value wrapper for entry is an object combined with its current decoder.
     * Allows converting of an intermediary object of a transcoder into the requested transcoder.
     * <br>
     * Useful when dealing with objects that have the same type required as their transcoder
     * for example NBT and JSON.
     */
    sealed interface RawValue permits CodecImpl.RawValueImpl {
        /**
         * Creates a RawValue instance
         *
         * @param coder the transcoder
         * @param value the value
         * @param <D>   The Object type
         * @return the new raw value instance
         */
        @Contract(pure = true, value = "_, _ -> new")
        static <D> RawValue of(Transcoder<D> coder, D value) {
            return new CodecImpl.RawValueImpl<>(coder, value);
        }

        /**
         * Converts the current value into another transcoder
         *
         * @param coder the transcoder to convert the object into
         * @param <D>   the resultant type; transcoder type.
         * @return the {@link Result} of converting to {@code coder}.
         */
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

    /**
     * Creates an enum codec from a given class
     * <br>
     * Converts the {@link Enum#name()} into lowercase when encoding
     * and uppercase into decoding then passing it to {@link Enum#valueOf(Class, String)}
     *
     * @param enumClass the enum class
     * @param <E>       Enum type, E must be an enum
     * @return the codec enum
     */
    @Contract(pure = true, value = "_ -> new")
    static <E extends Enum<E>> Codec<E> Enum(Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        return STRING.transform(
                value -> Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Create a recursive codec from the parent codec
     * <br>
     * Useful when you want to keep encoding/decoding until there is nothing left.
     *
     * @param func the function to get the codec from.
     * @param <T>  The codec Type
     * @return the recursive codec
     */
    @Contract(pure = true, value = "_ -> new")
    static <T> Codec<T> Recursive(Function<Codec<T>, Codec<T>> func) {
        return new CodecImpl.RecursiveImpl<>(func).delegate;
    }

    /**
     * Lazily gets the reference of a codec; considered immutably lazy.
     * <br>
     * Useful for breaking possible cyclic loading of recursive codecs.
     * This may become a stable value in the future; don't rely on supplier getting called multiple times.
     *
     * @param supplier the supplier to load the codec from.
     * @param <T>      the codec type
     * @return the supplier
     */
    @Contract(pure = true, value = "_ -> new")
    static <T> Codec<T> ForwardRef(Supplier<Codec<T>> supplier) {
        return new CodecImpl.ForwardRefImpl<>(supplier);
    }

    /**
     * Shortcut for {@link Codec#RegistryTaggedUnion(Registries.Selector, Function, String)}
     *
     * @param registry         the codec registry
     * @param serializerGetter the codec getter
     * @param key              the map key
     * @param <T>              the struct codec type.
     * @return a {@link StructCodec}
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    static <T> StructCodec<T> RegistryTaggedUnion(
            Registry<StructCodec<? extends T>> registry,
            Function<T, StructCodec<? extends T>> serializerGetter,
            String key
    ) {
        Objects.requireNonNull(registry, "registry");
        return Codec.RegistryTaggedUnion((ignored) -> registry, serializerGetter, key);
    }

    /**
     * Creates a {@link StructCodec} to bidirectionally map values of {@link T} to their encoded values
     * <br>
     * Registry selectors will be used to lookup values of codecs of {@link T}.
     * Then will be used to map to object {@link T} from {@code key}
     *
     * @param registrySelector the registry selector used during lookup.
     * @param serializerGetter the serializer for each value of {@link T}
     * @param key              the map key for {@link T}
     * @param <T>              the codec type
     * @return a {@link StructCodec} bidirectionally mapping values of {@link T}
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    static <T> StructCodec<T> RegistryTaggedUnion(
            Registries.Selector<StructCodec<? extends T>> registrySelector,
            Function<T, StructCodec<? extends T>> serializerGetter,
            String key
    ) {
        return new CodecImpl.RegistryTaggedUnionImpl<>(registrySelector, serializerGetter, key);
    }

    /**
     * Creates an Either Codec, depending on the value of Either decides which codec to use.
     *
     * @param leftCodec  the left codec
     * @param rightCodec the right codec
     * @param <L>        the left type
     * @param <R>        the right type
     * @return a {@link Codec} with {@link Either} of {@link L} and {@link R}
     */
    @Contract(pure = true, value = "_, _ -> new")
    static <L, R> Codec<Either<L, R>> Either(Codec<L> leftCodec, Codec<R> rightCodec) {
        return new CodecImpl.EitherImpl<>(leftCodec, rightCodec);
    }

    /**
     * Creates an optional codec, where null is encodable into {@link Transcoder#createNull()}.
     *
     * @return the optional codec of type {@link T}
     */
    @Contract(pure = true, value = "-> new")
    default Codec<@Nullable T> optional() {
        return new CodecImpl.OptionalImpl<>(this, null);
    }

    /**
     * Creates an optional codec, where null is encodable
     * and is encoded when value equals {@code defaultValue} or null through {@link Transcoder#createNull()}.
     * <br>
     * The default value will be used if the decoding is null or fails to decode.
     *
     * @param defaultValue the default value
     * @return the optional codec of type {@link T}
     * @throws NullPointerException if defaultValue is null, use {@link #optional()} instead.
     */
    @Contract(pure = true, value = "_ -> new")
    default Codec<@UnknownNullability T> optional(T defaultValue) {
        // We really have no idea what nullability this will have as optional still accepts null values, but the default value could never be null
        return new CodecImpl.OptionalImpl<>(this, Objects.requireNonNull(defaultValue, "Default value cannot be null"));
    }

    /**
     * Transforms an object from {@link S} to another {@link T} and from {@link T} back to {@link S}
     *
     * @param to   the function to {@link S} from {@link T}
     * @param from the function from {@link S} to {@link T}
     * @param <S>  the type
     * @return the transforming codec of {@link S}
     */
    @Contract(pure = true, value = "_, _ -> new")
    default <S extends @UnknownNullability Object> Codec<S> transform(ThrowingFunction<T, S> to, ThrowingFunction<S, T> from) {
        return new CodecImpl.TransformImpl<>(this, to, from);
    }

    /**
     * Creates an unmodifiable list codec of {@link T} where its size is no larger than {@code maxSize}.
     *
     * @param maxSize the max size of the list before returning an error result.
     * @return the list codec of type {@link T}
     */
    @Contract(pure = true, value = "_ -> new")
    default Codec<@Unmodifiable List<T>> list(int maxSize) {
        return new CodecImpl.ListImpl<>(this, maxSize);
    }

    /**
     * Creates an unmodifiable unbounded list codec. See {@link #list(int)}
     *
     * @return the unbounded list codec of type {@link T}
     */
    @Contract(pure = true, value = "-> new")
    default Codec<@Unmodifiable List<T>> list() {
        return list(Integer.MAX_VALUE);
    }

    /**
     * Returns an unmodifiable list or the first element or null if no such element exists.
     *
     * @param maxSize the max size of the list before returning an error result
     * @return the list codec of type {@link T}
     */
    @Contract(pure = true, value = "_ -> new")
    default Codec<@Unmodifiable @Nullable List<T>> listOrSingle(int maxSize) {
        return Codec.this.list(maxSize).orElse(Codec.this.transform(
                (it) -> it == null ? null : List.of(it), list -> list.isEmpty() ? null : list.getFirst()));
    }

    /**
     * Returns an unmodifiable unbounded list or the first element or null if no such element exists.
     * See {@link #listOrSingle(int)}
     *
     * @return the list codec of type {@link T}
     */
    @Contract(pure = true, value = "-> new")
    default Codec<@Unmodifiable @Nullable List<T>> listOrSingle() {
        return this.listOrSingle(Integer.MAX_VALUE);
    }

    /**
     * Creates an unmodifiable set where its max is no larger than {@code maxSize}
     *
     * @param maxSize the max size before returning an error result
     * @return the set codec of type {@link T}
     */
    @Contract(pure = true, value = "_ -> new")
    default Codec<@Unmodifiable Set<T>> set(int maxSize) {
        return new CodecImpl.SetImpl<>(Codec.this, maxSize);
    }

    /**
     * Creates an unmodifiable unbounded set. See {@link #set(int)}
     *
     * @return the set codec of type {@link T}
     */
    @Contract(pure = true, value = "-> new")
    default Codec<@Unmodifiable Set<T>> set() {
        return set(Integer.MAX_VALUE);
    }

    /**
     * Creates an unmodifiable map of key {@link T} and value of {@link V}
     *
     * @param valueCodec the codec to use for {@link V}
     * @param maxSize    the max size before returning an error result.
     * @param <V>        the value type
     * @return the map codec of type {@link T} and {@link V}
     */
    @Contract(pure = true, value = "_, _ -> new")
    default <V> Codec<@Unmodifiable Map<T, V>> mapValue(Codec<V> valueCodec, int maxSize) {
        return new CodecImpl.MapImpl<>(Codec.this, valueCodec, maxSize);
    }

    /**
     * Creates an unmodifiable map of key {@link T} and value of {@link V}. See {@link #mapValue(Codec, int)}
     *
     * @param valueCodec the codec to use for {@link V}
     * @param <V>        the value type
     * @return the map codec of type {@link T} and {@link V}
     */
    @Contract(pure = true, value = "_ -> new")
    default <V> Codec<@Unmodifiable Map<T, V>> mapValue(Codec<V> valueCodec) {
        return mapValue(valueCodec, Integer.MAX_VALUE);
    }

    /**
     * Creates a union type of type {@link R}. See {@link #unionType(String, Function, Function)}
     * <br>
     * Useful when you have an interface of {@link T} and want a codec subclasses of {@link T}
     *
     * @param serializers the map from {@link T} value to its serializer
     * @param keyFunc     to map from {@link R} to its value of {@link T}
     * @param <R>         the return type; {@link T} or a subclass
     * @return the struct codec union of {@link R}
     */
    @Contract(pure = true, value = "_, _ -> new")
    default <R> StructCodec<R> unionType(Function<T, StructCodec<? extends R>> serializers, Function<R, ? extends T> keyFunc) {
        return unionType("type", serializers, keyFunc);
    }

    /**
     * Creates a union type of type {@link R}
     * <br>
     * Useful when you have an interface of {@link T} and want a codec subclasses of {@link T}
     *
     * @param keyField    the map key
     * @param serializers the map from {@link T} value to its serializer
     * @param keyFunc     to map from {@link R} to its value of {@link T}
     * @param <R>         the return type; {@link T} or a subclass
     * @return the struct codec union of {@link R}
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    default <R> StructCodec<R> unionType(
            String keyField,
            Function<T, StructCodec<? extends R>> serializers,
            Function<R, ? extends T> keyFunc
    ) {
        return new CodecImpl.UnionImpl<>(keyField, this, serializers, keyFunc);
    }

    /**
     * Creates a or else codec where it will attempt to use the first codec
     * then use the second one if it fails.
     * <br>
     * If both codecs fail the first error will be returned instead.
     *
     * @param other the other codec
     * @return the or else codec of {@link T}
     */
    @Contract(pure = true, value = "_ -> new")
    default Codec<T> orElse(Codec<T> other) {
        return new CodecImpl.OrElseImpl<>(this, other);
    }

}
