package net.minestom.server.codec;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32C;

final class TranscoderCrc32Impl implements Transcoder<Integer> {
    static final TranscoderCrc32Impl INSTANCE = new TranscoderCrc32Impl();

    private static final Comparator<Map.Entry<Integer, Integer>> KEY_COMPARATOR = Map.Entry.comparingByKey(Comparator.comparingLong(Integer::toUnsignedLong));
    private static final Comparator<Map.Entry<Integer, Integer>> VALUE_COMPARATOR = Map.Entry.comparingByValue(Comparator.comparingLong(Integer::toUnsignedLong));
    private static final Comparator<Map.Entry<Integer, Integer>> MAP_COMPARATOR = KEY_COMPARATOR.thenComparing(VALUE_COMPARATOR);

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
    public Integer createNull() {
        return EMPTY;
    }

    @Override
    public Integer createBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public Integer createByte(byte value) {
        return new Hasher().putByte(TAG_BYTE).putByte(value).hash();
    }

    @Override
    public Integer createShort(short value) {
        return new Hasher().putByte(TAG_SHORT).putShort(value).hash();
    }

    @Override
    public Integer createInt(int value) {
        return new Hasher().putByte(TAG_INT).putInt(value).hash();
    }

    @Override
    public Integer createLong(long value) {
        return new Hasher().putByte(TAG_LONG).putLong(value).hash();
    }

    @Override
    public Integer createFloat(float value) {
        return new Hasher().putByte(TAG_FLOAT).putFloat(value).hash();
    }

    @Override
    public Integer createDouble(double value) {
        return new Hasher().putByte(TAG_DOUBLE).putDouble(value).hash();
    }

    @Override
    public Integer createString(String value) {
        return new Hasher().putByte(TAG_STRING)
                .putInt(value.length())
                .putChars(value)
                .hash();
    }

    @Override
    public Integer emptyList() {
        return EMPTY_LIST;
    }

    @Override
    public ListBuilder<Integer> createList(int expectedSize) {
        final Hasher hasher = new Hasher().putByte(TAG_LIST_START);
        return new ListBuilder<>() {
            @Override
            public ListBuilder<Integer> add(Integer value) {
                hasher.putIntBytes(value);
                return this;
            }

            @Override
            public Integer build() {
                return hasher.putByte(TAG_LIST_END).hash();
            }
        };
    }

    @Override
    public Integer emptyMap() {
        return EMPTY_MAP;
    }

    @Override
    public MapBuilder<Integer> createMap() {
        final HashMap<Integer, Integer> map = new HashMap<>();
        return new MapBuilder<>() {
            @Override
            public MapBuilder<Integer> put(Integer key, Integer value) {
                if (value != EMPTY)
                    map.put(key, value);
                return this;
            }

            @Override
            public MapBuilder<Integer> put(String key, Integer value) {
                return put(createString(key), value);
            }

            @Override
            public Integer build() {
                if (map.isEmpty()) return EMPTY_MAP;
                final Hasher hasher = new Hasher().putByte(TAG_MAP_START);
                map.entrySet().stream().sorted(MAP_COMPARATOR).forEach(entry -> {
                    hasher.putIntBytes(entry.getKey());
                    hasher.putIntBytes(entry.getValue());
                });
                return hasher.putByte(TAG_MAP_END).hash();
            }
        };
    }

    @Override
    public Integer createByteArray(byte[] value) {
        return new Hasher().putByte(TAG_BYTE_ARRAY_START)
                .putBytes(value).putByte(TAG_BYTE_ARRAY_END).hash();
    }

    @Override
    public Integer createIntArray(int[] value) {
        final Hasher hasher = new Hasher().putByte(TAG_INT_ARRAY_START);
        for (final int item : value) hasher.putInt(item);
        return hasher.putByte(TAG_INT_ARRAY_END).hash();
    }

    @Override
    public Integer createLongArray(long[] value) {
        final Hasher hasher = new Hasher().putByte(TAG_LONG_ARRAY_START);
        for (final long item : value) hasher.putLong(item);
        return hasher.putByte(TAG_LONG_ARRAY_END).hash();
    }

    // Noop read implementation below

    @Override
    public Result<Boolean> getBoolean(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<Byte> getByte(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<Short> getShort(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<Integer> getInt(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<Long> getLong(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<Float> getFloat(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<Double> getDouble(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<String> getString(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<byte[]> getByteArray(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<int[]> getIntArray(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<long[]> getLongArray(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<List<Integer>> getList(Integer value) {
        return writeOnly();
    }

    @Override
    public Result<MapLike<Integer>> getMap(Integer value) {
        return writeOnly();
    }

    @Override
    public <O> Result<O> convertTo(Transcoder<O> coder, Integer value) {
        return writeOnly();
    }

    private static <T> Result<T> writeOnly() {
        return new Result.Error<>("CRC32 transcoder only supports encoding");
    }


    // Loosely based on the Hasher implementation from Guava, licensed under the Apache 2.0 license.
    private record Hasher(CRC32C crc32, ByteBuffer buffer) {
        public Hasher() {
            this(new CRC32C(), ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN));
        }

        private Hasher update(int bytes) {
            crc32.update(buffer.array(), 0, bytes);
            buffer.position(0);
            return this;
        }

        public Hasher putByte(byte b) {
            crc32.update(b);
            return this;
        }

        public Hasher putShort(short s) {
            buffer.putShort(s);
            return update(Short.BYTES);
        }

        public Hasher putInt(int i) {
            buffer.putInt(i);
            return update(Integer.BYTES);
        }

        public Hasher putIntBytes(int i) {
            putByte((byte) i);
            putByte((byte) (i >> 8));
            putByte((byte) (i >> 16));
            putByte((byte) (i >> 24));
            return this;
        }

        public Hasher putLong(long l) {
            buffer.putLong(l);
            return update(Long.BYTES);
        }

        public Hasher putFloat(float f) {
            return putInt(Float.floatToRawIntBits(f));
        }

        public Hasher putDouble(double d) {
            return putLong(Double.doubleToRawLongBits(d));
        }

        public Hasher putChar(char c) {
            this.putByte((byte) c);
            this.putByte((byte) (c >>> 8));
            return this;
        }

        public Hasher putChars(String string) {
            for (int i = 0; i < string.length(); ++i)
                this.putChar(string.charAt(i));
            return this;
        }

        public Hasher putBytes(byte[] bytes) {
            crc32.update(bytes);
            return this;
        }

        public int hash() {
            return (int) crc32.getValue();
        }
    }
}
