package net.minestom.server.codec;

import com.google.gson.JsonElement;
import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public interface Transcoder<D> {

    @NotNull Transcoder<BinaryTag> NBT = TranscoderNbtImpl.INSTANCE;
    @NotNull Transcoder<JsonElement> JSON = TranscoderJsonImpl.INSTANCE;
    @NotNull Transcoder<Integer> CRC32_HASH = TranscoderCrc32Impl.INSTANCE;

    @NotNull D createNull();

    @NotNull Result<Boolean> getBoolean(@NotNull D value);

    @NotNull D createBoolean(boolean value);

    @NotNull Result<Byte> getByte(@NotNull D value);

    @NotNull D createByte(byte value);

    @NotNull Result<Short> getShort(@NotNull D value);

    @NotNull D createShort(short value);

    @NotNull Result<Integer> getInt(@NotNull D value);

    @NotNull D createInt(int value);

    @NotNull Result<Long> getLong(@NotNull D value);

    @NotNull D createLong(long value);

    @NotNull Result<Float> getFloat(@NotNull D value);

    @NotNull D createFloat(float value);

    @NotNull Result<Double> getDouble(@NotNull D value);

    @NotNull D createDouble(double value);

    @NotNull Result<String> getString(@NotNull D value);

    @NotNull D createString(@NotNull String value);

    @NotNull Result<List<D>> getList(@NotNull D value);

    @NotNull D createList(@NotNull List<D> value);

    boolean hasValue(@NotNull D value, @NotNull String key);

    @NotNull Result<D> getValue(@NotNull D value, @NotNull String key);

    @NotNull MapBuilder<D> createMap();

    default @NotNull Result<byte[]> getByteArray(@NotNull D value) {
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

    default @NotNull D createByteArray(byte[] value) {
        final List<D> list = new ArrayList<>(value.length);
        for (byte b : value) list.add(createByte(b));
        return createList(list);
    }

    default @NotNull Result<int[]> getIntArray(@NotNull D value) {
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

    default @NotNull D createIntArray(int[] value) {
        final List<D> list = new ArrayList<>(value.length);
        for (int i : value) list.add(createInt(i));
        return createList(list);
    }

    default @NotNull Result<long[]> getLongArray(@NotNull D value) {
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

    default @NotNull D createLongArray(long[] value) {
        final List<D> list = new ArrayList<>(value.length);
        for (long l : value) list.add(createLong(l));
        return createList(list);
    }

    interface ListBuilder {

    }

    interface MapBuilder<D> {
        void put(@NotNull String key, D value);

        D build();
    }

}
