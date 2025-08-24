package net.minestom.server.codec;

import com.google.gson.JsonElement;
import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

@ApiStatus.Experimental
public interface Transcoder<D> {

    Transcoder<BinaryTag> NBT = TranscoderNbtImpl.INSTANCE;
    Transcoder<JsonElement> JSON = TranscoderJsonImpl.INSTANCE;
    Transcoder<Object> JAVA = TranscoderJavaImpl.INSTANCE;
    Transcoder<Integer> CRC32_HASH = TranscoderCrc32Impl.INSTANCE;

    D createNull();

    Result<Boolean> getBoolean(D value);

    D createBoolean(boolean value);

    Result<Byte> getByte(D value);

    D createByte(byte value);

    Result<Short> getShort(D value);

    D createShort(short value);

    Result<Integer> getInt(D value);

    D createInt(int value);

    Result<Long> getLong(D value);

    D createLong(long value);

    Result<Float> getFloat(D value);

    D createFloat(float value);

    Result<Double> getDouble(D value);

    D createDouble(double value);

    Result<String> getString(D value);

    D createString(String value);

    Result<List<D>> getList(D value);

    default D emptyList() {
        return createList(0).build();
    }

    ListBuilder<D> createList(int expectedSize);

    Result<MapLike<D>> getMap(D value);

    default D emptyMap() {
        return createMap().build();
    }

    MapBuilder<D> createMap();

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

    default D createByteArray(byte[] value) {
        final ListBuilder<D> list = createList(value.length);
        for (byte b : value) list.add(createByte(b));
        return list.build();
    }

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

    default D createIntArray(int[] value) {
        final ListBuilder<D> list = createList(value.length);
        for (int i : value) list.add(createInt(i));
        return list.build();
    }

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

    default D createLongArray(long[] value) {
        final ListBuilder<D> list = createList(value.length);
        for (long l : value) list.add(createLong(l));
        return list.build();
    }

    <O> Result<O> convertTo(Transcoder<O> coder, D value);

    interface ListBuilder<D> {
        ListBuilder<D> add(D value);

        D build();
    }

    interface MapLike<D> {

        Collection<String> keys();

        boolean hasValue(String key);

        Result<D> getValue(String key);

        default int size() {
            return keys().size();
        }

        default boolean isEmpty() {
            return size() == 0;
        }
    }

    interface MapBuilder<D> {
        MapBuilder<D> put(D key, D value);

        MapBuilder<D> put(String key, D value);

        D build();
    }

}
