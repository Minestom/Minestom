package net.minestom.server.codec;

import net.kyori.adventure.nbt.*;

import java.util.*;

final class TranscoderNbtImpl implements Transcoder<BinaryTag> {
    static final TranscoderNbtImpl INSTANCE = new TranscoderNbtImpl();

    private static final Set<String> WRAPPED_ELEMENT_KEYS = Set.of("");

    @Override
    public BinaryTag createNull() {
        return EndBinaryTag.endBinaryTag();
    }

    @Override
    public Result<Boolean> getBoolean(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.byteValue() != 0)
                : new Result.Error<>("Not a boolean: " + value);
    }

    @Override
    public BinaryTag createBoolean(boolean value) {
        return value ? ByteBinaryTag.ONE : ByteBinaryTag.ZERO;
    }

    @Override
    public Result<Byte> getByte(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.byteValue())
                : new Result.Error<>("Not a byte: " + value);
    }

    @Override
    public BinaryTag createByte(byte value) {
        if (value == 0) return ByteBinaryTag.ZERO;
        if (value == 1) return ByteBinaryTag.ONE;
        return ByteBinaryTag.byteBinaryTag(value);
    }

    @Override
    public Result<Short> getShort(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.shortValue())
                : new Result.Error<>("Not a short: " + value);
    }

    @Override
    public BinaryTag createShort(short value) {
        return ShortBinaryTag.shortBinaryTag(value);
    }

    @Override
    public Result<Integer> getInt(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.intValue())
                : new Result.Error<>("Not an int: " + value);
    }

    @Override
    public BinaryTag createInt(int value) {
        return IntBinaryTag.intBinaryTag(value);
    }

    @Override
    public Result<Long> getLong(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.longValue())
                : new Result.Error<>("Not a long: " + value);
    }

    @Override
    public BinaryTag createLong(long value) {
        return LongBinaryTag.longBinaryTag(value);
    }

    @Override
    public Result<Float> getFloat(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.floatValue())
                : new Result.Error<>("Not a float: " + value);
    }

    @Override
    public BinaryTag createFloat(float value) {
        return FloatBinaryTag.floatBinaryTag(value);
    }

    @Override
    public Result<Double> getDouble(BinaryTag value) {
        return value instanceof NumberBinaryTag number
                ? new Result.Ok<>(number.doubleValue())
                : new Result.Error<>("Not a double: " + value);
    }

    @Override
    public BinaryTag createDouble(double value) {
        return DoubleBinaryTag.doubleBinaryTag(value);
    }

    @Override
    public Result<String> getString(BinaryTag value) {
        return value instanceof StringBinaryTag string
                ? new Result.Ok<>(string.value())
                : new Result.Error<>("Not a string: " + value);
    }

    @Override
    public BinaryTag createString(String value) {
        return StringBinaryTag.stringBinaryTag(value);
    }

    @Override
    public Result<List<BinaryTag>> getList(BinaryTag value) {
        if (!(value instanceof ListBinaryTag listTagWrapped))
            return new Result.Error<>("Not a list: " + value);
        final ListBinaryTag listTag = listTagWrapped.unwrapHeterogeneity();
        return new Result.Ok<>(new AbstractList<>() {
            @Override
            public BinaryTag get(int index) {
                return listTag.get(index);
            }

            @Override
            public int size() {
                return listTag.size();
            }
        });
    }

    @Override
    public BinaryTag emptyList() {
        return ListBinaryTag.empty();
    }

    @Override
    public ListBuilder<BinaryTag> createList(int expectedSize) {
        final ListBinaryTag.Builder<BinaryTag> elements = ListBinaryTag.heterogeneousListBinaryTag();
        return new ListBuilder<>() {
            @Override
            public ListBuilder<BinaryTag> add(BinaryTag value) {
                elements.add(value);
                return this;
            }

            @Override
            public BinaryTag build() {
                return elements.build();
            }
        };
    }

    @Override
    public Result<MapLike<BinaryTag>> getMap(BinaryTag value) {
        if (!(value instanceof CompoundBinaryTag compoundTag))
            return new Result.Error<>("Not a compound: " + value);
        return new Result.Ok<>(new MapLike<>() {
            @Override
            public Collection<String> keys() {
                return compoundTag.keySet();
            }

            @Override
            public boolean hasValue(String key) {
                return compoundTag.get(key) != null;
            }

            @Override
            public Result<BinaryTag> getValue(String key) {
                final BinaryTag tag = compoundTag.get(key);
                if (tag == null) return new Result.Error<>("No such key: " + key);
                return new Result.Ok<>(tag);
            }

            @Override
            public int size() {
                return compoundTag.size();
            }
        });
    }

    @Override
    public BinaryTag emptyMap() {
        return CompoundBinaryTag.empty();
    }

    @Override
    public MapBuilder<BinaryTag> createMap() {
        final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        return new MapBuilder<>() {
            @Override
            public MapBuilder<BinaryTag> put(BinaryTag key, BinaryTag value) {
                if (!(value instanceof EndBinaryTag) && key instanceof StringBinaryTag string)
                    builder.put(string.value(), value);
                return this;
            }

            @Override
            public MapBuilder<BinaryTag> put(String key, BinaryTag value) {
                if (!(value instanceof EndBinaryTag))
                    builder.put(key, value);
                return this;
            }

            @Override
            public BinaryTag build() {
                return builder.build();
            }
        };
    }

    @Override
    public Result<byte[]> getByteArray(BinaryTag value) {
        return value instanceof ByteArrayBinaryTag byteArray
                ? new Result.Ok<>(byteArray.value())
                : new Result.Error<>("Not a byte array: " + value);
    }

    @Override
    public BinaryTag createByteArray(byte[] value) {
        return ByteArrayBinaryTag.byteArrayBinaryTag(value);
    }

    @Override
    public Result<int[]> getIntArray(BinaryTag value) {
        return value instanceof IntArrayBinaryTag intArray
                ? new Result.Ok<>(intArray.value())
                : new Result.Error<>("Not an int array: " + value);
    }

    @Override
    public BinaryTag createIntArray(int[] value) {
        return IntArrayBinaryTag.intArrayBinaryTag(value);
    }

    @Override
    public Result<long[]> getLongArray(BinaryTag value) {
        return value instanceof LongArrayBinaryTag longArray
                ? new Result.Ok<>(longArray.value())
                : new Result.Error<>("Not a long array: " + value);
    }

    @Override
    public BinaryTag createLongArray(long[] value) {
        return LongArrayBinaryTag.longArrayBinaryTag(value);
    }

    @Override
    public <O> Result<O> convertTo(Transcoder<O> coder, BinaryTag value) {
        return switch (value) {
            case EndBinaryTag ignored -> new Result.Ok<>(coder.createNull());
            case ByteBinaryTag byteTag -> new Result.Ok<>(coder.createByte(byteTag.byteValue()));
            case ShortBinaryTag shortTag -> new Result.Ok<>(coder.createShort(shortTag.shortValue()));
            case IntBinaryTag intTag -> new Result.Ok<>(coder.createInt(intTag.intValue()));
            case LongBinaryTag longTag -> new Result.Ok<>(coder.createLong(longTag.longValue()));
            case FloatBinaryTag floatTag -> new Result.Ok<>(coder.createFloat(floatTag.floatValue()));
            case DoubleBinaryTag doubleTag -> new Result.Ok<>(coder.createDouble(doubleTag.doubleValue()));
            case ByteArrayBinaryTag byteArrayTag -> new Result.Ok<>(coder.createByteArray(byteArrayTag.value()));
            case StringBinaryTag stringTag -> new Result.Ok<>(coder.createString(stringTag.value()));
            case ListBinaryTag listTag -> {
                listTag = listTag.unwrapHeterogeneity();
                final ListBuilder<O> list = coder.createList(listTag.size());
                for (int i = 0; i < listTag.size(); i++) {
                    switch (convertTo(coder, listTag.get(i))) {
                        case Result.Ok<O> ok -> list.add(ok.value());
                        case Result.Error<O> error -> {
                            yield new Result.Error<>(i + ": " + error);
                        }
                    }
                }
                yield new Result.Ok<>(list.build());
            }
            case CompoundBinaryTag compoundTag -> {
                final MapBuilder<O> map = coder.createMap();
                for (Map.Entry<String, ? extends BinaryTag> entry : compoundTag) {
                    switch (convertTo(coder, entry.getValue())) {
                        case Result.Ok<O> ok -> map.put(coder.createString(entry.getKey()), ok.value());
                        case Result.Error<O> error -> {
                            yield new Result.Error<>(entry.getKey() + ": " + error);
                        }
                    }
                }
                yield new Result.Ok<>(map.build());
            }
            case IntArrayBinaryTag intArrayTag -> new Result.Ok<>(coder.createIntArray(intArrayTag.value()));
            case LongArrayBinaryTag longArrayTag -> new Result.Ok<>(coder.createLongArray(longArrayTag.value()));
            default -> new Result.Error<>("Unsupported type: " + value);
        };
    }
}
