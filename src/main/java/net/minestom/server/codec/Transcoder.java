package net.minestom.server.codec;

import com.google.gson.JsonElement;
import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

/**
 * Transcoders are responsible for converting "primitive" java objects into their respective {@link D} types.
 * They are also responsible for unwrapping these objects back to their primitives.
 * <br>
 * Commonly used transcoders are accessible through static fields like {@link Transcoder#JSON}
 * @param <D> the intermediary type used by the transcoder
 */
public interface Transcoder<D> {

    Transcoder<BinaryTag> NBT = TranscoderNbtImpl.INSTANCE;
    Transcoder<JsonElement> JSON = TranscoderJsonImpl.INSTANCE;
    Transcoder<Object> JAVA = TranscoderJavaImpl.INSTANCE;
    @ApiStatus.Experimental
    Transcoder<Integer> CRC32_HASH = TranscoderCrc32Impl.INSTANCE;

    /**
     * Creates a null representation of {@link D}
     * @return the null object, never {@code null}
     */
    D createNull();

    /**
     * Attempts to unwrap a boolean from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Boolean> getBoolean(D value);

    /**
     * Creates a boolean representation of {@link D}
     * @param value the boolean primitive
     * @return the representation of value in {@link D}
     */
    D createBoolean(boolean value);

    /**
     * Attempts to unwrap a byte from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Byte> getByte(D value);

    /**
     * Creates a byte representation of {@link D}
     * @param value the byte primitive
     * @return the representation of value in {@link D}
     */
    D createByte(byte value);

    /**
     * Attempts to unwrap a short from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Short> getShort(D value);

    /**
     * Creates a short representation of {@link D}
     * @param value the short primitive
     * @return the representation of value in {@link D}
     */
    D createShort(short value);

    /**
     * Attempts to unwrap an int from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Integer> getInt(D value);

    /**
     * Creates an int representation of {@link D}
     * @param value the int primitive
     * @return the representation of value in {@link D}
     */
    D createInt(int value);

    /**
     * Attempts to unwrap a long from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Long> getLong(D value);

    /**
     * Creates a long representation of {@link D}
     * @param value the long primitive
     * @return the representation of value in {@link D}
     */
    D createLong(long value);

    /**
     * Attempts to unwrap a float from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Float> getFloat(D value);

    /**
     * Creates a float representation of {@link D}
     * @param value the float primitive
     * @return the representation of value in {@link D}
     */
    D createFloat(float value);

    /**
     * Attempts to unwrap a double from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<Double> getDouble(D value);

    /**
     * Creates a float representation of {@link D}
     * @param value the float primitive
     * @return the representation of value in {@link D}
     */
    D createDouble(double value);

    /**
     * Attempts to unwrap a string from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    Result<String> getString(D value);

    /**
     * Creates a string representation of {@link D}
     * @param value the string primitive
     * @return the representation of value in {@link D}
     */
    D createString(String value);

    /**
     * Attempts to unwrap a list from the value {@link D}
     * <br>
     * The {@link List} decoded possibly has more of {@link D} contained inside.
     * @param value the value to unwrap
     * @return the result
     */
    Result<@Unmodifiable List<D>> getList(D value);

    /**
     * A empty list intermediary
     * @return the empty list intermediary
     */
    default D emptyList() {
        return createList(0).build();
    }

    /**
     * Creates a {@link ListBuilder}
     * @param expectedSize the initial size
     * @return a list builder
     */
    @Contract(pure = true)
    ListBuilder<D> createList(int expectedSize);

    /**
     * Attempts to unwrap a map from the value {@link D}
     * <br>
     * The {@link MapLike} decoded possibly has more of {@link D} contained inside.
     * @param value the value to unwrap
     * @return the result
     */
    Result<MapLike<D>> getMap(D value);

    /**
     * A emtpy map intermediary
     * @return the empty map intermediary
     */
    default D emptyMap() {
        return createMap().build();
    }

    /**
     * Creates a {@link MapBuilder}
     * @return a new {@link MapBuilder}
     */
    @Contract(pure = true)
    MapBuilder<D> createMap();

    /**
     * Attempts to unwrap a {@code byte[]} from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    default Result<byte[]> getByteArray(D value) {
        final Result<List<D>> listResult = getList(value);
        if (!(listResult instanceof Result.Ok(List<D> list)))
            return listResult.cast();
        final byte[] byteArray = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            final Result<Byte> byteResult = getByte(list.get(i));
            if (!(byteResult instanceof Result.Ok(Byte byteValue)))
                return byteResult.cast();
            byteArray[i] = byteValue;
        }
        return new Result.Ok<>(byteArray);
    }

    /**
     * Creates a {@code byte[]} representation of {@link D}
     * @param value the byte array
     * @return {@link D} representation of {@code byte[]}
     */
    default D createByteArray(byte[] value) {
        final ListBuilder<D> list = createList(value.length);
        for (byte b : value) list.add(createByte(b));
        return list.build();
    }

    /**
     * Attempts to unwrap a {@code int[]} from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    default Result<int[]> getIntArray(D value) {
        final Result<List<D>> listResult = getList(value);
        if (!(listResult instanceof Result.Ok(List<D> list)))
            return listResult.cast();
        final int[] intArray = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            final Result<Integer> intResult = getInt(list.get(i));
            if (!(intResult instanceof Result.Ok(Integer intValue)))
                return intResult.cast();
            intArray[i] = intValue;
        }
        return new Result.Ok<>(intArray);
    }

    /**
     * Creates a {@code int[]} representation of {@link D}
     * @param value the int array
     * @return {@link D} representation of {@code int[]}
     */
    default D createIntArray(int[] value) {
        final ListBuilder<D> list = createList(value.length);
        for (int i : value) list.add(createInt(i));
        return list.build();
    }

    /**
     * Attempts to unwrap a {@code long[]} from the value {@link D}
     * @param value the value to unwrap
     * @return the result
     */
    default Result<long[]> getLongArray(D value) {
        final Result<List<D>> listResult = getList(value);
        if (!(listResult instanceof Result.Ok(List<D> list)))
            return listResult.cast();
        final long[] longArray = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            final Result<Long> longResult = getLong(list.get(i));
            if (!(longResult instanceof Result.Ok(Long longValue)))
                return longResult.cast();
            longArray[i] = longValue;
        }
        return new Result.Ok<>(longArray);
    }

    /**
     * Creates a {@code long[]} representation of {@link D}
     * @param value the long array
     * @return {@link D} representation of {@code long[]}
     */
    default D createLongArray(long[] value) {
        final ListBuilder<D> list = createList(value.length);
        for (long l : value) list.add(createLong(l));
        return list.build();
    }

    /**
     * Converts the current intermediary of {@link D} into intermediary {@link O}
     * @param coder the transcoder to convert to
     * @param value the value to convert
     * @return the resultant of the conversion
     * @param <O> the intermediary type to convert to
     */
    <O> Result<O> convertTo(Transcoder<O> coder, D value);

    /**
     * List builders are used to eventually build a list.
     * <br>
     * They are considered mutable containers, but provide builder semantics.
     * @param <D> the transcoder type
     */
    interface ListBuilder<D> {
        ListBuilder<D> add(D value);

        D build();
    }

    /**
     * Represents an immutable {@link java.util.Map} like object.
     * @param <D> the transcoder type
     */
    interface MapLike<D> {

        /**
         * Gets all the keys
         * @return the collection of keys
         */
        @Contract(pure = true)
        @Unmodifiable Collection<String> keys();

        /**
         * Checks if the map has the value mapped to the key
         * @param key the key to check
         * @return true if present; false otherwise
         */
        @Contract(pure = true)
        boolean hasValue(String key);

        /**
         * Gets the value of the key in a result.
         * <br>
         * Check if the key has a value using {@link #hasValue(String)}
         * @param key the key to use
         * @return the result, {@link Result.Error} if missing
         */
        Result<D> getValue(String key);

        /**
         * @return the size of the map
         */
        @Contract(pure = true)
        default int size() {
            return keys().size();
        }

        /**
         * @return true if the size is zero
         */
        @Contract(pure = true)
        default boolean isEmpty() {
            return size() == 0;
        }
    }

    /**
     * Map builders are used to eventually build a map
     * <br>
     * They are considered mutable containers, but provide builder semantics.
     * @param <D> the type of object used by the transcoder
     */
    interface MapBuilder<D> {

        /**
         * Puts an entry onto the map
         * @param key the key
         * @param value the value
         * @return this
         */
        MapBuilder<D> put(D key, D value);

        /**
         * Puts an entry onto the map
         * @param key the string key
         * @param value the value
         * @return this
         */
        MapBuilder<D> put(String key, D value);

        /**
         * Build the map with the current values
         * @return the completed map of type {@link D}
         */
        D build();
    }

}
