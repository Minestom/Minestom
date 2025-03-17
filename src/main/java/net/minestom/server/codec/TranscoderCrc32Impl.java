package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

final class TranscoderCrc32Impl implements Transcoder<Integer> {
    static final TranscoderCrc32Impl INSTANCE = new TranscoderCrc32Impl();

    private static final Comparator<Map.Entry<Integer, Integer>> KEY_COMPARATOR = Map.Entry.comparingByKey(Integer::compare);
    private static final Comparator<Map.Entry<Integer, Integer>> VALUE_COMPARATOR = Map.Entry.comparingByValue(Integer::compare);
    private static final Comparator<Map.Entry<Integer, Integer>> COMPARATOR = KEY_COMPARATOR.thenComparing(VALUE_COMPARATOR);

    private static final byte TAG_EMPTY = 1;
    private static final byte TAG_MAP_START = 2;
    private static final byte TAG_MAP_END = 3;
    private static final byte TAG_LIST_START = 4;
    private static final byte TAG_LIST_END = 5;
    private static final byte TAG_BYTE = 6;
    private static final byte TAG_SHORT = 7;
    private static final byte TAG_INT = 8;
    private static final byte TAG_LONG = 9;
    private static final byte TAG_FLOAT = 10;
    private static final byte TAG_DOUBLE = 11;
    private static final byte TAG_STRING = 12;
    private static final byte TAG_BOOLEAN = 13;
    private static final byte TAG_BYTE_ARRAY_START = 14;
    private static final byte TAG_BYTE_ARRAY_END = 15;
    private static final byte TAG_INT_ARRAY_START = 16;
    private static final byte TAG_INT_ARRAY_END = 17;
    private static final byte TAG_LONG_ARRAY_START = 18;
    private static final byte TAG_LONG_ARRAY_END = 19;

    private static final int EMPTY = new Hasher().putByte(TAG_EMPTY).hash();
    private static final int EMPTY_MAP = new Hasher().putByte(TAG_MAP_START).putByte(TAG_MAP_END).hash();
    private static final int EMPTY_LIST = new Hasher().putByte(TAG_LIST_START).putByte(TAG_LIST_END).hash();
    private static final int FALSE = new Hasher().putByte(TAG_BOOLEAN).putByte((byte) 0).hash();
    private static final int TRUE = new Hasher().putByte(TAG_BOOLEAN).putByte((byte) 1).hash();

    @Override
    public @NotNull Integer createNull() {
        return EMPTY;
    }

    @Override
    public @NotNull Integer createBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public @NotNull Integer createByte(byte value) {
        return new Hasher().putByte(TAG_BYTE).putByte(value).hash();
    }

    @Override
    public @NotNull Integer createShort(short value) {
        return new Hasher().putByte(TAG_SHORT).putShort(value).hash();
    }

    @Override
    public @NotNull Integer createInt(int value) {
        return new Hasher().putByte(TAG_INT).putInt(value).hash();
    }

    @Override
    public @NotNull Integer createLong(long value) {
        return new Hasher().putByte(TAG_LONG).putLong(value).hash();
    }

    @Override
    public @NotNull Integer createFloat(float value) {
        return new Hasher().putFloat(TAG_FLOAT).putFloat(value).hash();
    }

    @Override
    public @NotNull Integer createDouble(double value) {
        return new Hasher().putDouble(TAG_DOUBLE).putDouble(value).hash();
    }

    @Override
    public @NotNull Integer createString(@NotNull String value) {
        return new Hasher().putByte(TAG_STRING)
                .putInt(value.length())
                .putBytes(value.getBytes(StandardCharsets.UTF_8)).hash();
    }

    @Override
    public @NotNull Integer createList(@NotNull List<Integer> value) {
        if (value.isEmpty()) return EMPTY_LIST;
        final Hasher hasher = new Hasher().putByte(TAG_LIST_START);
        for (final Integer item : value) hasher.putInt(item);
        return hasher.putByte(TAG_LIST_END).hash();
    }

    @Override
    public @NotNull MapBuilder<Integer> createMap() {
        final HashMap<Integer, Integer> map = new HashMap<>();
        return new MapBuilder<>() {
            @Override
            public void put(@NotNull String key, Integer value) {
                map.put(createString(key), value);
            }

            @Override
            public Integer build() {
                if (map.isEmpty()) return EMPTY_MAP;
                final Hasher hasher = new Hasher().putByte(TAG_MAP_START);
                map.entrySet().stream().sorted(COMPARATOR).forEach(entry -> {
                    hasher.putInt(entry.getKey());
                    hasher.putInt(entry.getValue());
                });
                return hasher.putByte(TAG_MAP_END).hash();
            }
        };
    }

    @Override
    public @NotNull Integer createByteArray(byte[] value) {
        return new Hasher().putByte(TAG_BYTE_ARRAY_START)
                .putBytes(value).putByte(TAG_BYTE_ARRAY_END).hash();
    }

    @Override
    public @NotNull Integer createIntArray(int[] value) {
        final Hasher hasher = new Hasher().putByte(TAG_INT_ARRAY_START);
        for (final int item : value) hasher.putInt(item);
        return hasher.putByte(TAG_INT_ARRAY_END).hash();
    }

    @Override
    public @NotNull Integer createLongArray(long[] value) {
        final Hasher hasher = new Hasher().putByte(TAG_LONG_ARRAY_START);
        for (final long item : value) hasher.putLong(item);
        return hasher.putByte(TAG_LONG_ARRAY_END).hash();
    }

    // Noop read implementation below

    @Override
    public @NotNull Result<Boolean> getBoolean(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<Byte> getByte(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<Short> getShort(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<Integer> getInt(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<Long> getLong(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<Float> getFloat(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<Double> getDouble(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<String> getString(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public @NotNull Result<List<Integer>> getList(@NotNull Integer value) {
        return writeOnly();
    }

    @Override
    public boolean hasValue(@NotNull Integer value, @NotNull String key) {
        return false;
    }

    @Override
    public @NotNull Result<Integer> getValue(@NotNull Integer value, @NotNull String key) {
        return writeOnly();
    }

    private static <T> @NotNull Result<T> writeOnly() {
        return new Result.Error<>("CRC32 transcoder only supports encoding");
    }


    // Loosely based on the Hasher implementation from Guava, licensed under the Apache 2.0 license.
    private record Hasher(@NotNull CRC32 crc32, @NotNull ByteBuffer buffer) {
        public Hasher() {
            this(new CRC32(), ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN));
        }

        private @NotNull Hasher update(int bytes) {
            buffer.limit(bytes);
            crc32.update(buffer);
            buffer.position(0);
            buffer.limit(8);
            return this;
        }

        public @NotNull Hasher putByte(byte b) {
            crc32.update(new byte[]{b});
            return this;
        }

        public @NotNull Hasher putShort(short s) {
            buffer.putShort(s);
            return update(Short.BYTES);
        }

        public @NotNull Hasher putInt(int i) {
            buffer.putInt(i);
            return update(Integer.BYTES);
        }

        public @NotNull Hasher putLong(long l) {
            buffer.putLong(l);
            return update(Long.BYTES);
        }

        public @NotNull Hasher putFloat(float f) {
            buffer.putFloat(f);
            return update(Float.BYTES);
        }

        public @NotNull Hasher putDouble(double d) {
            buffer.putDouble(d);
            return update(Double.BYTES);
        }

        public @NotNull Hasher putBytes(byte[] bytes) {
            crc32.update(bytes);
            return this;
        }

        public int hash() {
            return (int) crc32.getValue();
        }
    }
}
