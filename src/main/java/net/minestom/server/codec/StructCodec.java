package net.minestom.server.codec;

import net.minestom.server.codec.Transcoder.MapBuilder;
import net.minestom.server.codec.Transcoder.MapLike;
import net.minestom.server.network.NetworkBufferTemplate.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A struct codec is a map backed {@link Codec}, where the keys are strings.
 * See {@link Codec}, {@link Decoder} and {@link Encoder}
 * <br>
 * You can also use {@link #struct(String, Codec, Function, F1)} to create as templating
 * similar to {@link net.minestom.server.network.NetworkBufferTemplate}
 * <p>
 * {@inheritDoc}
 * <br>
 * You can use structs to create complex objects
 * <pre>{@code
 * record MyObject(double coolnessFactor, @Nullable String of) {
 *     static final StructCodec<MyObject> CODEC = StructCodec.struct(
 *             "id", Codec.DOUBLE, MyObject::coolnessFactor,
 *             "name", Codec.STRING.optional(), MyObject::of,
 *             MyObject::new
 *     );
 *
 *     public MyObject {
 *         coolnessFactor = Math.clamp(coolnessFactor, 0.0, 2.0); // Too powerful
 *     }
 * }
 *
 * MyObject value = new MyObject(7.8d, "me"); // Or use a null name for no name.
 * // Encoding to JSON
 * JsonElement encoded = MyObject.CODEC.encode(Transcoder.JSON, value).orElseThrow();
 * // Decoding from JSON
 * MyObject decoded = MyObject.CODEC.decode(Transcoder.JSON, encoded).orElseThrow();
 * }</pre>
 *
 * @param <R> the return type, never null.
 */
public interface StructCodec<R> extends Codec<R> {
    /**
     * A special key used to instruct the codec to inline the value instead of wrapping it in a map.
     * The inlined codec must also be a StructCodec.
     */
    String INLINE = "$$inline$$";

    /**
     * Decode a value {@link R} from the backing map of {@link D}
     *
     * @param coder the transcoder for {@link D}
     * @param map   the map to decode from
     * @param <D>   the transcoder type
     * @return the result of decoding
     */
    <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map);

    /**
     * Decode a value {@link R} into the backing map of {@link D}
     *
     * @param coder the transcoder for {@link D}
     * @param value the value of {@link R} to encode
     * @param map   the map to decode from
     * @param <D>   the transcoder type
     * @return the result of encoding
     */
    <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map);

    /**
     * {@inheritDoc}
     *
     * @param coder the transcoder to use
     * @param value the value to decode
     * @param <D>   the transcoder type
     * @return the result from decoding
     */
    @Override
    default <D> Result<R> decode(Transcoder<D> coder, D value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return coder.getMap(value).map(map -> decodeFromMap(coder, map));
    }

    /**
     * {@inheritDoc}
     *
     * @param coder the transcoder to use
     * @param value the value to encode, if null returns error
     * @param <D>   the transcoder type
     * @return the result from encoding
     */
    @Override
    default <D> Result<D> encode(Transcoder<D> coder, @Nullable R value) {
        if (value == null) return new Result.Error<>("null");
        return encodeToMap(coder, value, coder.createMap());
    }

    /**
     * Similar to {@link #orElse(Codec)} but uses the map backing instead.
     * <br>
     * For decoding it attempts to use the current codec or uses the other codec,
     * if neither work returns the firsts error.
     *
     * @param other the other struct codec
     * @return the new or else struct
     */
    default StructCodec<R> orElseStruct(StructCodec<R> other) {
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<R> primaryResult = StructCodec.this.decodeFromMap(coder, map);
                if (primaryResult instanceof Result.Ok<R> primaryOk)
                    return primaryOk;

                // Primary did not work, try secondary
                final Result<R> secondaryResult = other.decodeFromMap(coder, map);
                if (secondaryResult instanceof Result.Ok<R> secondaryOk)
                    return secondaryOk;

                // Secondary did not work either, return error from primary.
                return primaryResult;
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                return StructCodec.this.encodeToMap(coder, value, map);
            }
        };
    }

    /**
     * Returns the value in any struct.
     *
     * @param value the value to return of {@link R}
     * @param <R>   the return type
     * @return the new struct codec for value
     */
    static <R> StructCodec<R> struct(R value) {
        final Result<R> ok = new Result.Ok<>(Objects.requireNonNull(value, "value"));
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                return ok;
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Lazily returns the value in any struct.
     *
     * @param ctor the value to return of {@link R}
     * @param <R>  the return type
     * @return the new struct codec for value
     */
    static <R> StructCodec<R> struct(Supplier<R> ctor) {
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                return new Result.Ok<>(ctor.get());
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            F1<P1, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                return new Result.Ok<>(ctor.apply(value1));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            F2<P1, P2, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                return new Result.Ok<>(ctor.apply(value1, value2));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            F3<P1, P2, P3, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param name4   the name/key for {@link P4}
     * @param codec4  the codec for {@link P4}
     * @param getter4 the getter for {@link P4}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @param <P4>    the fourth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            F4<P1, P2, P3, P4, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param name4   the name/key for {@link P4}
     * @param codec4  the codec for {@link P4}
     * @param getter4 the getter for {@link P4}
     * @param name5   the name/key for {@link P5}
     * @param codec5  the codec for {@link P5}
     * @param getter5 the getter for {@link P5}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @param <P4>    the fourth parameter type
     * @param <P5>    the fifth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            F5<P1, P2, P3, P4, P5, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param name4   the name/key for {@link P4}
     * @param codec4  the codec for {@link P4}
     * @param getter4 the getter for {@link P4}
     * @param name5   the name/key for {@link P5}
     * @param codec5  the codec for {@link P5}
     * @param getter5 the getter for {@link P5}
     * @param name6   the name/key for {@link P6}
     * @param codec6  the codec for {@link P6}
     * @param getter6 the getter for {@link P6}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @param <P4>    the fourth parameter type
     * @param <P5>    the fifth parameter type
     * @param <P6>    the sixth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            F6<P1, P2, P3, P4, P5, P6, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param name4   the name/key for {@link P4}
     * @param codec4  the codec for {@link P4}
     * @param getter4 the getter for {@link P4}
     * @param name5   the name/key for {@link P5}
     * @param codec5  the codec for {@link P5}
     * @param getter5 the getter for {@link P5}
     * @param name6   the name/key for {@link P6}
     * @param codec6  the codec for {@link P6}
     * @param getter6 the getter for {@link P6}
     * @param name7   the name/key for {@link P7}
     * @param codec7  the codec for {@link P7}
     * @param getter7 the getter for {@link P7}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @param <P4>    the fourth parameter type
     * @param <P5>    the fifth parameter type
     * @param <P6>    the sixth parameter type
     * @param <P7>    the seventh parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            F7<P1, P2, P3, P4, P5, P6, P7, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                return new Result.Ok<>(map.build());
            }
        };
    }


    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param name4   the name/key for {@link P4}
     * @param codec4  the codec for {@link P4}
     * @param getter4 the getter for {@link P4}
     * @param name5   the name/key for {@link P5}
     * @param codec5  the codec for {@link P5}
     * @param getter5 the getter for {@link P5}
     * @param name6   the name/key for {@link P6}
     * @param codec6  the codec for {@link P6}
     * @param getter6 the getter for {@link P6}
     * @param name7   the name/key for {@link P7}
     * @param codec7  the codec for {@link P7}
     * @param getter7 the getter for {@link P7}
     * @param name8   the name/key for {@link P8}
     * @param codec8  the codec for {@link P8}
     * @param getter8 the getter for {@link P8}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @param <P4>    the fourth parameter type
     * @param <P5>    the fifth parameter type
     * @param <P6>    the sixth parameter type
     * @param <P7>    the seventh parameter type
     * @param <P8>    the eighth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            F8<P1, P2, P3, P4, P5, P6, P7, P8, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1   the name/key for {@link P1}
     * @param codec1  the codec for {@link P1}
     * @param getter1 the getter for {@link P1}
     * @param name2   the name/key for {@link P2}
     * @param codec2  the codec for {@link P2}
     * @param getter2 the getter for {@link P2}
     * @param name3   the name/key for {@link P3}
     * @param codec3  the codec for {@link P3}
     * @param getter3 the getter for {@link P3}
     * @param name4   the name/key for {@link P4}
     * @param codec4  the codec for {@link P4}
     * @param getter4 the getter for {@link P4}
     * @param name5   the name/key for {@link P5}
     * @param codec5  the codec for {@link P5}
     * @param getter5 the getter for {@link P5}
     * @param name6   the name/key for {@link P6}
     * @param codec6  the codec for {@link P6}
     * @param getter6 the getter for {@link P6}
     * @param name7   the name/key for {@link P7}
     * @param codec7  the codec for {@link P7}
     * @param getter7 the getter for {@link P7}
     * @param name8   the name/key for {@link P8}
     * @param codec8  the codec for {@link P8}
     * @param getter8 the getter for {@link P8}
     * @param name9   the name/key for {@link P9}
     * @param codec9  the codec for {@link P9}
     * @param getter9 the getter for {@link P9}
     * @param ctor    the constructor for {@link R}
     * @param <R>     the return type
     * @param <P1>    the first parameter type
     * @param <P2>    the second parameter type
     * @param <P3>    the third parameter type
     * @param <P4>    the fourth parameter type
     * @param <P5>    the fifth parameter type
     * @param <P6>    the sixth parameter type
     * @param <P7>    the seventh parameter type
     * @param <P8>    the eighth parameter type
     * @param <P9>    the ninth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            F11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                return new Result.Ok<>(map.build());
            }
        };
    }


    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            F12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            F13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            F14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param name15   the name/key for {@link P15}
     * @param codec15  the codec for {@link P15}
     * @param getter15 the getter for {@link P15}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @param <P15>    the fifteenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object, P15 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            String name15, Codec<P15> codec15, Function<R, P15> getter15,
            F15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(name15, "name15");
        Objects.requireNonNull(codec15, "codec15");
        Objects.requireNonNull(getter15, "getter15");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                final Result<P15> result15 = get(coder, codec15, name15, map);
                if (!(result15 instanceof Result.Ok(P15 value15)))
                    return result15.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                final Result<D> result15 = put(coder, codec15, map, name15, getter15.apply(value));
                if (result15 != null) return result15;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param name15   the name/key for {@link P15}
     * @param codec15  the codec for {@link P15}
     * @param getter15 the getter for {@link P15}
     * @param name16   the name/key for {@link P16}
     * @param codec16  the codec for {@link P16}
     * @param getter16 the getter for {@link P16}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @param <P15>    the fifteenth parameter type
     * @param <P16>    the sixteenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object, P15 extends @UnknownNullability Object, P16 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            String name15, Codec<P15> codec15, Function<R, P15> getter15,
            String name16, Codec<P16> codec16, Function<R, P16> getter16,
            F16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(name15, "name15");
        Objects.requireNonNull(codec15, "codec15");
        Objects.requireNonNull(getter15, "getter15");
        Objects.requireNonNull(name16, "name16");
        Objects.requireNonNull(codec16, "codec16");
        Objects.requireNonNull(getter16, "getter16");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                final Result<P15> result15 = get(coder, codec15, name15, map);
                if (!(result15 instanceof Result.Ok(P15 value15)))
                    return result15.cast();
                final Result<P16> result16 = get(coder, codec16, name16, map);
                if (!(result16 instanceof Result.Ok(P16 value16)))
                    return result16.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15, value16));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                final Result<D> result15 = put(coder, codec15, map, name15, getter15.apply(value));
                if (result15 != null) return result15;
                final Result<D> result16 = put(coder, codec16, map, name16, getter16.apply(value));
                if (result16 != null) return result16;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param name15   the name/key for {@link P15}
     * @param codec15  the codec for {@link P15}
     * @param getter15 the getter for {@link P15}
     * @param name16   the name/key for {@link P16}
     * @param codec16  the codec for {@link P16}
     * @param getter16 the getter for {@link P16}
     * @param name17   the name/key for {@link P17}
     * @param codec17  the codec for {@link P17}
     * @param getter17 the getter for {@link P17}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @param <P15>    the fifteenth parameter type
     * @param <P16>    the sixteenth parameter type
     * @param <P17>    the seventeenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object, P15 extends @UnknownNullability Object, P16 extends @UnknownNullability Object, P17 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            String name15, Codec<P15> codec15, Function<R, P15> getter15,
            String name16, Codec<P16> codec16, Function<R, P16> getter16,
            String name17, Codec<P17> codec17, Function<R, P17> getter17,
            F17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(name15, "name15");
        Objects.requireNonNull(codec15, "codec15");
        Objects.requireNonNull(getter15, "getter15");
        Objects.requireNonNull(name16, "name16");
        Objects.requireNonNull(codec16, "codec16");
        Objects.requireNonNull(getter16, "getter16");
        Objects.requireNonNull(name17, "name17");
        Objects.requireNonNull(codec17, "codec17");
        Objects.requireNonNull(getter17, "getter17");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                final Result<P15> result15 = get(coder, codec15, name15, map);
                if (!(result15 instanceof Result.Ok(P15 value15)))
                    return result15.cast();
                final Result<P16> result16 = get(coder, codec16, name16, map);
                if (!(result16 instanceof Result.Ok(P16 value16)))
                    return result16.cast();
                final Result<P17> result17 = get(coder, codec17, name17, map);
                if (!(result17 instanceof Result.Ok(P17 value17)))
                    return result17.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15, value16, value17));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                final Result<D> result15 = put(coder, codec15, map, name15, getter15.apply(value));
                if (result15 != null) return result15;
                final Result<D> result16 = put(coder, codec16, map, name16, getter16.apply(value));
                if (result16 != null) return result16;
                final Result<D> result17 = put(coder, codec17, map, name17, getter17.apply(value));
                if (result17 != null) return result17;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param name15   the name/key for {@link P15}
     * @param codec15  the codec for {@link P15}
     * @param getter15 the getter for {@link P15}
     * @param name16   the name/key for {@link P16}
     * @param codec16  the codec for {@link P16}
     * @param getter16 the getter for {@link P16}
     * @param name17   the name/key for {@link P17}
     * @param codec17  the codec for {@link P17}
     * @param getter17 the getter for {@link P17}
     * @param name18   the name/key for {@link P18}
     * @param codec18  the codec for {@link P18}
     * @param getter18 the getter for {@link P18}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @param <P15>    the fifteenth parameter type
     * @param <P16>    the sixteenth parameter type
     * @param <P17>    the seventeenth parameter type
     * @param <P18>    the eighteenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object, P15 extends @UnknownNullability Object, P16 extends @UnknownNullability Object, P17 extends @UnknownNullability Object, P18 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            String name15, Codec<P15> codec15, Function<R, P15> getter15,
            String name16, Codec<P16> codec16, Function<R, P16> getter16,
            String name17, Codec<P17> codec17, Function<R, P17> getter17,
            String name18, Codec<P18> codec18, Function<R, P18> getter18,
            F18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(name15, "name15");
        Objects.requireNonNull(codec15, "codec15");
        Objects.requireNonNull(getter15, "getter15");
        Objects.requireNonNull(name16, "name16");
        Objects.requireNonNull(codec16, "codec16");
        Objects.requireNonNull(getter16, "getter16");
        Objects.requireNonNull(name17, "name17");
        Objects.requireNonNull(codec17, "codec17");
        Objects.requireNonNull(getter17, "getter17");
        Objects.requireNonNull(name18, "name18");
        Objects.requireNonNull(codec18, "codec18");
        Objects.requireNonNull(getter18, "getter18");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                final Result<P15> result15 = get(coder, codec15, name15, map);
                if (!(result15 instanceof Result.Ok(P15 value15)))
                    return result15.cast();
                final Result<P16> result16 = get(coder, codec16, name16, map);
                if (!(result16 instanceof Result.Ok(P16 value16)))
                    return result16.cast();
                final Result<P17> result17 = get(coder, codec17, name17, map);
                if (!(result17 instanceof Result.Ok(P17 value17)))
                    return result17.cast();
                final Result<P18> result18 = get(coder, codec18, name18, map);
                if (!(result18 instanceof Result.Ok(P18 value18)))
                    return result18.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15, value16, value17, value18));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                final Result<D> result15 = put(coder, codec15, map, name15, getter15.apply(value));
                if (result15 != null) return result15;
                final Result<D> result16 = put(coder, codec16, map, name16, getter16.apply(value));
                if (result16 != null) return result16;
                final Result<D> result17 = put(coder, codec17, map, name17, getter17.apply(value));
                if (result17 != null) return result17;
                final Result<D> result18 = put(coder, codec18, map, name18, getter18.apply(value));
                if (result18 != null) return result18;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param name15   the name/key for {@link P15}
     * @param codec15  the codec for {@link P15}
     * @param getter15 the getter for {@link P15}
     * @param name16   the name/key for {@link P16}
     * @param codec16  the codec for {@link P16}
     * @param getter16 the getter for {@link P16}
     * @param name17   the name/key for {@link P17}
     * @param codec17  the codec for {@link P17}
     * @param getter17 the getter for {@link P17}
     * @param name18   the name/key for {@link P18}
     * @param codec18  the codec for {@link P18}
     * @param getter18 the getter for {@link P18}
     * @param name19   the name/key for {@link P19}
     * @param codec19  the codec for {@link P19}
     * @param getter19 the getter for {@link P19}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @param <P15>    the fifteenth parameter type
     * @param <P16>    the sixteenth parameter type
     * @param <P17>    the seventeenth parameter type
     * @param <P18>    the eighteenth parameter type
     * @param <P19>    the nineteenth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object, P15 extends @UnknownNullability Object, P16 extends @UnknownNullability Object, P17 extends @UnknownNullability Object, P18 extends @UnknownNullability Object, P19 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            String name15, Codec<P15> codec15, Function<R, P15> getter15,
            String name16, Codec<P16> codec16, Function<R, P16> getter16,
            String name17, Codec<P17> codec17, Function<R, P17> getter17,
            String name18, Codec<P18> codec18, Function<R, P18> getter18,
            String name19, Codec<P19> codec19, Function<R, P19> getter19,
            F19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(name15, "name15");
        Objects.requireNonNull(codec15, "codec15");
        Objects.requireNonNull(getter15, "getter15");
        Objects.requireNonNull(name16, "name16");
        Objects.requireNonNull(codec16, "codec16");
        Objects.requireNonNull(getter16, "getter16");
        Objects.requireNonNull(name17, "name17");
        Objects.requireNonNull(codec17, "codec17");
        Objects.requireNonNull(getter17, "getter17");
        Objects.requireNonNull(name18, "name18");
        Objects.requireNonNull(codec18, "codec18");
        Objects.requireNonNull(getter18, "getter18");
        Objects.requireNonNull(name19, "name19");
        Objects.requireNonNull(codec19, "codec19");
        Objects.requireNonNull(getter19, "getter19");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                final Result<P15> result15 = get(coder, codec15, name15, map);
                if (!(result15 instanceof Result.Ok(P15 value15)))
                    return result15.cast();
                final Result<P16> result16 = get(coder, codec16, name16, map);
                if (!(result16 instanceof Result.Ok(P16 value16)))
                    return result16.cast();
                final Result<P17> result17 = get(coder, codec17, name17, map);
                if (!(result17 instanceof Result.Ok(P17 value17)))
                    return result17.cast();
                final Result<P18> result18 = get(coder, codec18, name18, map);
                if (!(result18 instanceof Result.Ok(P18 value18)))
                    return result18.cast();
                final Result<P19> result19 = get(coder, codec19, name19, map);
                if (!(result19 instanceof Result.Ok(P19 value19)))
                    return result19.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15, value16, value17, value18, value19));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                final Result<D> result15 = put(coder, codec15, map, name15, getter15.apply(value));
                if (result15 != null) return result15;
                final Result<D> result16 = put(coder, codec16, map, name16, getter16.apply(value));
                if (result16 != null) return result16;
                final Result<D> result17 = put(coder, codec17, map, name17, getter17.apply(value));
                if (result17 != null) return result17;
                final Result<D> result18 = put(coder, codec18, map, name18, getter18.apply(value));
                if (result18 != null) return result18;
                final Result<D> result19 = put(coder, codec19, map, name19, getter19.apply(value));
                if (result19 != null) return result19;
                return new Result.Ok<>(map.build());
            }
        };
    }

    /**
     * Creates a struct template. See {@link StructCodec}
     *
     * @param name1    the name/key for {@link P1}
     * @param codec1   the codec for {@link P1}
     * @param getter1  the getter for {@link P1}
     * @param name2    the name/key for {@link P2}
     * @param codec2   the codec for {@link P2}
     * @param getter2  the getter for {@link P2}
     * @param name3    the name/key for {@link P3}
     * @param codec3   the codec for {@link P3}
     * @param getter3  the getter for {@link P3}
     * @param name4    the name/key for {@link P4}
     * @param codec4   the codec for {@link P4}
     * @param getter4  the getter for {@link P4}
     * @param name5    the name/key for {@link P5}
     * @param codec5   the codec for {@link P5}
     * @param getter5  the getter for {@link P5}
     * @param name6    the name/key for {@link P6}
     * @param codec6   the codec for {@link P6}
     * @param getter6  the getter for {@link P6}
     * @param name7    the name/key for {@link P7}
     * @param codec7   the codec for {@link P7}
     * @param getter7  the getter for {@link P7}
     * @param name8    the name/key for {@link P8}
     * @param codec8   the codec for {@link P8}
     * @param getter8  the getter for {@link P8}
     * @param name9    the name/key for {@link P9}
     * @param codec9   the codec for {@link P9}
     * @param getter9  the getter for {@link P9}
     * @param name10   the name/key for {@link P10}
     * @param codec10  the codec for {@link P10}
     * @param getter10 the getter for {@link P10}
     * @param name11   the name/key for {@link P11}
     * @param codec11  the codec for {@link P11}
     * @param getter11 the getter for {@link P11}
     * @param name12   the name/key for {@link P12}
     * @param codec12  the codec for {@link P12}
     * @param getter12 the getter for {@link P12}
     * @param name13   the name/key for {@link P13}
     * @param codec13  the codec for {@link P13}
     * @param getter13 the getter for {@link P13}
     * @param name14   the name/key for {@link P14}
     * @param codec14  the codec for {@link P14}
     * @param getter14 the getter for {@link P14}
     * @param name15   the name/key for {@link P15}
     * @param codec15  the codec for {@link P15}
     * @param getter15 the getter for {@link P15}
     * @param name16   the name/key for {@link P16}
     * @param codec16  the codec for {@link P16}
     * @param getter16 the getter for {@link P16}
     * @param name17   the name/key for {@link P17}
     * @param codec17  the codec for {@link P17}
     * @param getter17 the getter for {@link P17}
     * @param name18   the name/key for {@link P18}
     * @param codec18  the codec for {@link P18}
     * @param getter18 the getter for {@link P18}
     * @param name19   the name/key for {@link P19}
     * @param codec19  the codec for {@link P19}
     * @param getter19 the getter for {@link P19}
     * @param name20   the name/key for {@link P20}
     * @param codec20  the codec for {@link P20}
     * @param getter20 the getter for {@link P20}
     * @param ctor     the constructor for {@link R}
     * @param <R>      the return type
     * @param <P1>     the first parameter type
     * @param <P2>     the second parameter type
     * @param <P3>     the third parameter type
     * @param <P4>     the fourth parameter type
     * @param <P5>     the fifth parameter type
     * @param <P6>     the sixth parameter type
     * @param <P7>     the seventh parameter type
     * @param <P8>     the eighth parameter type
     * @param <P9>     the ninth parameter type
     * @param <P10>    the tenth parameter type
     * @param <P11>    the eleventh parameter type
     * @param <P12>    the twelfth parameter type
     * @param <P13>    the thirteenth parameter type
     * @param <P14>    the fourteenth parameter type
     * @param <P15>    the fifteenth parameter type
     * @param <P16>    the sixteenth parameter type
     * @param <P17>    the seventeenth parameter type
     * @param <P18>    the eighteenth parameter type
     * @param <P19>    the nineteenth parameter type
     * @param <P20>    the twentieth parameter type
     * @return the new {@link StructCodec} template.
     */
    static <R, P1 extends @UnknownNullability Object, P2 extends @UnknownNullability Object, P3 extends @UnknownNullability Object, P4 extends @UnknownNullability Object, P5 extends @UnknownNullability Object, P6 extends @UnknownNullability Object, P7 extends @UnknownNullability Object, P8 extends @UnknownNullability Object, P9 extends @UnknownNullability Object, P10 extends @UnknownNullability Object, P11 extends @UnknownNullability Object, P12 extends @UnknownNullability Object, P13 extends @UnknownNullability Object, P14 extends @UnknownNullability Object, P15 extends @UnknownNullability Object, P16 extends @UnknownNullability Object, P17 extends @UnknownNullability Object, P18 extends @UnknownNullability Object, P19 extends @UnknownNullability Object, P20 extends @UnknownNullability Object> StructCodec<R> struct(
            String name1, Codec<P1> codec1, Function<R, P1> getter1,
            String name2, Codec<P2> codec2, Function<R, P2> getter2,
            String name3, Codec<P3> codec3, Function<R, P3> getter3,
            String name4, Codec<P4> codec4, Function<R, P4> getter4,
            String name5, Codec<P5> codec5, Function<R, P5> getter5,
            String name6, Codec<P6> codec6, Function<R, P6> getter6,
            String name7, Codec<P7> codec7, Function<R, P7> getter7,
            String name8, Codec<P8> codec8, Function<R, P8> getter8,
            String name9, Codec<P9> codec9, Function<R, P9> getter9,
            String name10, Codec<P10> codec10, Function<R, P10> getter10,
            String name11, Codec<P11> codec11, Function<R, P11> getter11,
            String name12, Codec<P12> codec12, Function<R, P12> getter12,
            String name13, Codec<P13> codec13, Function<R, P13> getter13,
            String name14, Codec<P14> codec14, Function<R, P14> getter14,
            String name15, Codec<P15> codec15, Function<R, P15> getter15,
            String name16, Codec<P16> codec16, Function<R, P16> getter16,
            String name17, Codec<P17> codec17, Function<R, P17> getter17,
            String name18, Codec<P18> codec18, Function<R, P18> getter18,
            String name19, Codec<P19> codec19, Function<R, P19> getter19,
            String name20, Codec<P20> codec20, Function<R, P20> getter20,
            F20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R> ctor
    ) {
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(codec1, "codec1");
        Objects.requireNonNull(getter1, "getter1");
        Objects.requireNonNull(name2, "name2");
        Objects.requireNonNull(codec2, "codec2");
        Objects.requireNonNull(getter2, "getter2");
        Objects.requireNonNull(name3, "name3");
        Objects.requireNonNull(codec3, "codec3");
        Objects.requireNonNull(getter3, "getter3");
        Objects.requireNonNull(name4, "name4");
        Objects.requireNonNull(codec4, "codec4");
        Objects.requireNonNull(getter4, "getter4");
        Objects.requireNonNull(name5, "name5");
        Objects.requireNonNull(codec5, "codec5");
        Objects.requireNonNull(getter5, "getter5");
        Objects.requireNonNull(name6, "name6");
        Objects.requireNonNull(codec6, "codec6");
        Objects.requireNonNull(getter6, "getter6");
        Objects.requireNonNull(name7, "name7");
        Objects.requireNonNull(codec7, "codec7");
        Objects.requireNonNull(getter7, "getter7");
        Objects.requireNonNull(name8, "name8");
        Objects.requireNonNull(codec8, "codec8");
        Objects.requireNonNull(getter8, "getter8");
        Objects.requireNonNull(name9, "name9");
        Objects.requireNonNull(codec9, "codec9");
        Objects.requireNonNull(getter9, "getter9");
        Objects.requireNonNull(name10, "name10");
        Objects.requireNonNull(codec10, "codec10");
        Objects.requireNonNull(getter10, "getter10");
        Objects.requireNonNull(name11, "name11");
        Objects.requireNonNull(codec11, "codec11");
        Objects.requireNonNull(getter11, "getter11");
        Objects.requireNonNull(name12, "name12");
        Objects.requireNonNull(codec12, "codec12");
        Objects.requireNonNull(getter12, "getter12");
        Objects.requireNonNull(name13, "name13");
        Objects.requireNonNull(codec13, "codec13");
        Objects.requireNonNull(getter13, "getter13");
        Objects.requireNonNull(name14, "name14");
        Objects.requireNonNull(codec14, "codec14");
        Objects.requireNonNull(getter14, "getter14");
        Objects.requireNonNull(name15, "name15");
        Objects.requireNonNull(codec15, "codec15");
        Objects.requireNonNull(getter15, "getter15");
        Objects.requireNonNull(name16, "name16");
        Objects.requireNonNull(codec16, "codec16");
        Objects.requireNonNull(getter16, "getter16");
        Objects.requireNonNull(name17, "name17");
        Objects.requireNonNull(codec17, "codec17");
        Objects.requireNonNull(getter17, "getter17");
        Objects.requireNonNull(name18, "name18");
        Objects.requireNonNull(codec18, "codec18");
        Objects.requireNonNull(getter18, "getter18");
        Objects.requireNonNull(name19, "name19");
        Objects.requireNonNull(codec19, "codec19");
        Objects.requireNonNull(getter19, "getter19");
        Objects.requireNonNull(name20, "name20");
        Objects.requireNonNull(codec20, "codec20");
        Objects.requireNonNull(getter20, "getter20");
        Objects.requireNonNull(ctor, "ctor");
        return new StructCodec<>() {
            @Override
            public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
                final Result<P1> result1 = get(coder, codec1, name1, map);
                if (!(result1 instanceof Result.Ok(P1 value1)))
                    return result1.cast();
                final Result<P2> result2 = get(coder, codec2, name2, map);
                if (!(result2 instanceof Result.Ok(P2 value2)))
                    return result2.cast();
                final Result<P3> result3 = get(coder, codec3, name3, map);
                if (!(result3 instanceof Result.Ok(P3 value3)))
                    return result3.cast();
                final Result<P4> result4 = get(coder, codec4, name4, map);
                if (!(result4 instanceof Result.Ok(P4 value4)))
                    return result4.cast();
                final Result<P5> result5 = get(coder, codec5, name5, map);
                if (!(result5 instanceof Result.Ok(P5 value5)))
                    return result5.cast();
                final Result<P6> result6 = get(coder, codec6, name6, map);
                if (!(result6 instanceof Result.Ok(P6 value6)))
                    return result6.cast();
                final Result<P7> result7 = get(coder, codec7, name7, map);
                if (!(result7 instanceof Result.Ok(P7 value7)))
                    return result7.cast();
                final Result<P8> result8 = get(coder, codec8, name8, map);
                if (!(result8 instanceof Result.Ok(P8 value8)))
                    return result8.cast();
                final Result<P9> result9 = get(coder, codec9, name9, map);
                if (!(result9 instanceof Result.Ok(P9 value9)))
                    return result9.cast();
                final Result<P10> result10 = get(coder, codec10, name10, map);
                if (!(result10 instanceof Result.Ok(P10 value10)))
                    return result10.cast();
                final Result<P11> result11 = get(coder, codec11, name11, map);
                if (!(result11 instanceof Result.Ok(P11 value11)))
                    return result11.cast();
                final Result<P12> result12 = get(coder, codec12, name12, map);
                if (!(result12 instanceof Result.Ok(P12 value12)))
                    return result12.cast();
                final Result<P13> result13 = get(coder, codec13, name13, map);
                if (!(result13 instanceof Result.Ok(P13 value13)))
                    return result13.cast();
                final Result<P14> result14 = get(coder, codec14, name14, map);
                if (!(result14 instanceof Result.Ok(P14 value14)))
                    return result14.cast();
                final Result<P15> result15 = get(coder, codec15, name15, map);
                if (!(result15 instanceof Result.Ok(P15 value15)))
                    return result15.cast();
                final Result<P16> result16 = get(coder, codec16, name16, map);
                if (!(result16 instanceof Result.Ok(P16 value16)))
                    return result16.cast();
                final Result<P17> result17 = get(coder, codec17, name17, map);
                if (!(result17 instanceof Result.Ok(P17 value17)))
                    return result17.cast();
                final Result<P18> result18 = get(coder, codec18, name18, map);
                if (!(result18 instanceof Result.Ok(P18 value18)))
                    return result18.cast();
                final Result<P19> result19 = get(coder, codec19, name19, map);
                if (!(result19 instanceof Result.Ok(P19 value19)))
                    return result19.cast();
                final Result<P20> result20 = get(coder, codec20, name20, map);
                if (!(result20 instanceof Result.Ok(P20 value20)))
                    return result20.cast();
                return new Result.Ok<>(ctor.apply(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15, value16, value17, value18, value19, value20));
            }

            @Override
            public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
                final Result<D> result1 = put(coder, codec1, map, name1, getter1.apply(value));
                if (result1 != null) return result1;
                final Result<D> result2 = put(coder, codec2, map, name2, getter2.apply(value));
                if (result2 != null) return result2;
                final Result<D> result3 = put(coder, codec3, map, name3, getter3.apply(value));
                if (result3 != null) return result3;
                final Result<D> result4 = put(coder, codec4, map, name4, getter4.apply(value));
                if (result4 != null) return result4;
                final Result<D> result5 = put(coder, codec5, map, name5, getter5.apply(value));
                if (result5 != null) return result5;
                final Result<D> result6 = put(coder, codec6, map, name6, getter6.apply(value));
                if (result6 != null) return result6;
                final Result<D> result7 = put(coder, codec7, map, name7, getter7.apply(value));
                if (result7 != null) return result7;
                final Result<D> result8 = put(coder, codec8, map, name8, getter8.apply(value));
                if (result8 != null) return result8;
                final Result<D> result9 = put(coder, codec9, map, name9, getter9.apply(value));
                if (result9 != null) return result9;
                final Result<D> result10 = put(coder, codec10, map, name10, getter10.apply(value));
                if (result10 != null) return result10;
                final Result<D> result11 = put(coder, codec11, map, name11, getter11.apply(value));
                if (result11 != null) return result11;
                final Result<D> result12 = put(coder, codec12, map, name12, getter12.apply(value));
                if (result12 != null) return result12;
                final Result<D> result13 = put(coder, codec13, map, name13, getter13.apply(value));
                if (result13 != null) return result13;
                final Result<D> result14 = put(coder, codec14, map, name14, getter14.apply(value));
                if (result14 != null) return result14;
                final Result<D> result15 = put(coder, codec15, map, name15, getter15.apply(value));
                if (result15 != null) return result15;
                final Result<D> result16 = put(coder, codec16, map, name16, getter16.apply(value));
                if (result16 != null) return result16;
                final Result<D> result17 = put(coder, codec17, map, name17, getter17.apply(value));
                if (result17 != null) return result17;
                final Result<D> result18 = put(coder, codec18, map, name18, getter18.apply(value));
                if (result18 != null) return result18;
                final Result<D> result19 = put(coder, codec19, map, name19, getter19.apply(value));
                if (result19 != null) return result19;
                final Result<D> result20 = put(coder, codec20, map, name20, getter20.apply(value));
                if (result20 != null) return result20;
                return new Result.Ok<>(map.build());
            }
        };
    }

    private static <D, T> Result<@UnknownNullability T> get(Transcoder<D> coder, Codec<T> codec, String key, MapLike<D> map) {
        if (INLINE.equals(key)) {
            final Codec<T> decodeCodec = codec instanceof CodecImpl.OptionalImpl<T>(
                    Codec<T> inner, T ignored
            ) ? inner : codec;
            if (!(decodeCodec instanceof StructCodec<T> s)) return new Result.Error<>(key + ": not a struct");

            final Result<T> decodeResult = s.decodeFromMap(coder, map);
            if (decodeResult instanceof Result.Error<T> && codec instanceof CodecImpl.OptionalImpl<T>(
                    Codec<T> ignored, T defaultValue
            )) return new Result.Ok<>(defaultValue);

            return decodeResult.mapError(e -> key + ": " + e);
        }
        if (codec instanceof CodecImpl.OptionalImpl<T>(Codec<T> inner, T defaultValue)) {
            return switch (map.getValue(key)) {
                case Result.Ok(D innerValue) -> inner.decode(coder, innerValue)
                        .mapError(e -> key + ": " + e);
                case Result.Error(String ignored) -> new Result.Ok<>(defaultValue);
            };
        }
        return map.getValue(key)
                .map(innerValue -> codec.decode(coder, innerValue))
                .mapError(e -> key + ": " + e);
    }

    private static <D, T> @Nullable Result<D> put(Transcoder<D> coder, Codec<T> codec, MapBuilder<D> map, String key, @Nullable T value) {
        if (value == null) {
            if (!(codec instanceof CodecImpl.OptionalImpl<T>))
                return new Result.Error<>(key + ": null");
            return null;
        }

        if (INLINE.equals(key)) {
            final Codec<T> encodeCodec = codec instanceof CodecImpl.OptionalImpl<T>(
                    Codec<T> inner, T ignored
            ) ? inner : codec;
            if (!(encodeCodec instanceof StructCodec<T> s))
                return new Result.Error<>(key + ": not a struct");
            final Result<D> mapEncodeResult = s.encodeToMap(coder, value, map);
            if (mapEncodeResult instanceof Result.Error<?> e)
                return new Result.Error<>(key + ": " + e);
            return null;
        }
        return switch (codec.encode(coder, value)) {
            case Result.Ok(D ok) -> {
                if (ok != null) map.put(key, ok);
                yield null;
            }
            case Result.Error(String message) -> new Result.Error<>(key + ": " + message);
        };
    }

}

