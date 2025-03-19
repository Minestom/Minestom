package net.minestom.server.codec;

import net.kyori.adventure.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class TranscoderNbtImpl implements Transcoder<BinaryTag> {
    static final TranscoderNbtImpl INSTANCE = new TranscoderNbtImpl();

    @Override
    public @NotNull BinaryTag createNull() {
        return EndBinaryTag.endBinaryTag();
    }

    @Override
    public @NotNull Result<Boolean> getBoolean(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.byteValue() != 0)
                : new Result.Error<>("Not a boolean: " + value);
    }

    @Override
    public @NotNull BinaryTag createBoolean(boolean value) {
        return value ? ByteBinaryTag.ONE : ByteBinaryTag.ZERO;
    }

    @Override
    public @NotNull Result<Byte> getByte(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.byteValue())
                : new Result.Error<>("Not a byte: " + value);
    }

    @Override
    public @NotNull BinaryTag createByte(byte value) {
        if (value == 0) return ByteBinaryTag.ZERO;
        if (value == 1) return ByteBinaryTag.ONE;
        return ByteBinaryTag.byteBinaryTag(value);
    }

    @Override
    public @NotNull Result<Short> getShort(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.shortValue())
                : new Result.Error<>("Not a short: " + value);
    }

    @Override
    public @NotNull BinaryTag createShort(short value) {
        return ShortBinaryTag.shortBinaryTag(value);
    }

    @Override
    public @NotNull Result<Integer> getInt(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.intValue())
                : new Result.Error<>("Not an int: " + value);
    }

    @Override
    public @NotNull BinaryTag createInt(int value) {
        return IntBinaryTag.intBinaryTag(value);
    }

    @Override
    public @NotNull Result<Long> getLong(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.longValue())
                : new Result.Error<>("Not a long: " + value);
    }

    @Override
    public @NotNull BinaryTag createLong(long value) {
        return LongBinaryTag.longBinaryTag(value);
    }

    @Override
    public @NotNull Result<Float> getFloat(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.floatValue())
                : new Result.Error<>("Not a float: " + value);
    }

    @Override
    public @NotNull BinaryTag createFloat(float value) {
        return FloatBinaryTag.floatBinaryTag(value);
    }

    @Override
    public @NotNull Result<Double> getDouble(@NotNull BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.doubleValue())
                : new Result.Error<>("Not a double: " + value);
    }

    @Override
    public @NotNull BinaryTag createDouble(double value) {
        return DoubleBinaryTag.doubleBinaryTag(value);
    }

    @Override
    public @NotNull Result<String> getString(@NotNull BinaryTag value) {
        return value instanceof StringBinaryTag string
                ? new Result.Ok<>(string.value())
                : new Result.Error<>("Not a string: " + value);
    }

    @Override
    public @NotNull BinaryTag createString(@NotNull String value) {
        return StringBinaryTag.stringBinaryTag(value);
    }

    @Override
    public @NotNull Result<List<BinaryTag>> getList(@NotNull BinaryTag value) {
        if (!(value instanceof ListBinaryTag listTag))
            return new Result.Error<>("Not a list: " + value);
        return new Result.Ok<>(listTag.stream().toList());
    }

    @Override
    public @NotNull BinaryTag createList(@NotNull List<BinaryTag> value) {
        if (value.isEmpty()) return ListBinaryTag.empty();
        return ListBinaryTag.from(value);
    }

    @Override
    public boolean hasValue(@NotNull BinaryTag value, @NotNull String key) {
        if (!(value instanceof CompoundBinaryTag compoundTag))
            return false;
        return compoundTag.get(key) != null;
    }

    @Override
    public @NotNull Result<BinaryTag> getValue(@NotNull BinaryTag value, @NotNull String key) {
        if (!(value instanceof CompoundBinaryTag compoundTag))
            return new Result.Error<>("Not a compound: " + value);
        final BinaryTag tag = compoundTag.get(key);
        if (tag == null) return new Result.Error<>("No such key: " + key);
        return new Result.Ok<>(tag);
    }

    @Override
    public @NotNull MapBuilder<BinaryTag> createMap() {
        final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        return new MapBuilder<>() {
            @Override
            public void put(@NotNull String key, BinaryTag value) {
                builder.put(key, value);
            }

            @Override
            public BinaryTag build() {
                return builder.build();
            }
        };
    }

    @Override
    public @NotNull Result<byte[]> getByteArray(@NotNull BinaryTag value) {
        return value instanceof ByteArrayBinaryTag byteArray
                ? new Result.Ok<>(byteArray.value())
                : new Result.Error<>("Not a byte array: " + value);
    }

    @Override
    public @NotNull BinaryTag createByteArray(byte[] value) {
        return ByteArrayBinaryTag.byteArrayBinaryTag(value);
    }

    @Override
    public @NotNull Result<int[]> getIntArray(@NotNull BinaryTag value) {
        return value instanceof IntArrayBinaryTag intArray
                ? new Result.Ok<>(intArray.value())
                : new Result.Error<>("Not an int array: " + value);
    }

    @Override
    public @NotNull BinaryTag createIntArray(int[] value) {
        return IntArrayBinaryTag.intArrayBinaryTag(value);
    }

    @Override
    public @NotNull Result<long[]> getLongArray(@NotNull BinaryTag value) {
        return value instanceof LongArrayBinaryTag longArray
                ? new Result.Ok<>(longArray.value())
                : new Result.Error<>("Not a long array: " + value);
    }

    @Override
    public @NotNull BinaryTag createLongArray(long[] value) {
        return LongArrayBinaryTag.longArrayBinaryTag(value);
    }
}
