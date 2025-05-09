package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;

import java.util.*;

final class TranscoderJavaImpl implements Transcoder<Object> {
    public static final Transcoder<Object> INSTANCE = new TranscoderJavaImpl();

    @Override
    public @NotNull Object createNull() {
        return Optional.empty();
    }

    @Override
    public @NotNull Result<Boolean> getBoolean(@NotNull Object value) {
        if (!(value instanceof Boolean b))
            return new Result.Error<>("Not a boolean: " + value);
        return new Result.Ok<>(b);
    }

    @Override
    public @NotNull Object createBoolean(boolean value) {
        return value;
    }

    @Override
    public @NotNull Result<Byte> getByte(@NotNull Object value) {
        if (!(value instanceof Number n))
            return new Result.Error<>("Not a byte: " + value);
        return new Result.Ok<>(n.byteValue());
    }

    @Override
    public @NotNull Object createByte(byte value) {
        return value;
    }

    @Override
    public @NotNull Result<Short> getShort(@NotNull Object value) {
        if (!(value instanceof Number n))
            return new Result.Error<>("Not a short: " + value);
        return new Result.Ok<>(n.shortValue());
    }

    @Override
    public @NotNull Object createShort(short value) {
        return value;
    }

    @Override
    public @NotNull Result<Integer> getInt(@NotNull Object value) {
        if (!(value instanceof Number n))
            return new Result.Error<>("Not an int: " + value);
        return new Result.Ok<>(n.intValue());
    }

    @Override
    public @NotNull Object createInt(int value) {
        return value;
    }

    @Override
    public @NotNull Result<Long> getLong(@NotNull Object value) {
        if (!(value instanceof Number n))
            return new Result.Error<>("Not a long: " + value);
        return new Result.Ok<>(n.longValue());
    }

    @Override
    public @NotNull Object createLong(long value) {
        return value;
    }

    @Override
    public @NotNull Result<Float> getFloat(@NotNull Object value) {
        if (!(value instanceof Number n))
            return new Result.Error<>("Not a float: " + value);
        return new Result.Ok<>(n.floatValue());
    }

    @Override
    public @NotNull Object createFloat(float value) {
        return value;
    }

    @Override
    public @NotNull Result<Double> getDouble(@NotNull Object value) {
        if (!(value instanceof Number n))
            return new Result.Error<>("Not a double: " + value);
        return new Result.Ok<>(n.doubleValue());
    }

    @Override
    public @NotNull Object createDouble(double value) {
        return value;
    }

    @Override
    public @NotNull Result<String> getString(@NotNull Object value) {
        if (!(value instanceof String s))
            return new Result.Error<>("Not a string: " + value);
        return new Result.Ok<>(s);
    }

    @Override
    public @NotNull Object createString(@NotNull String value) {
        return value;
    }

    @Override
    public @NotNull Result<List<Object>> getList(@NotNull Object value) {
        if (!(value instanceof List<?> list))
            return new Result.Error<>("Not a list: " + value);
        //noinspection unchecked
        return new Result.Ok<>((List<Object>) list);
    }

    @Override
    public @NotNull ListBuilder<Object> createList(int expectedSize) {
        final List<Object> list = new java.util.ArrayList<>(expectedSize);
        return new ListBuilder<>() {
            @Override
            public @NotNull ListBuilder<Object> add(Object value) {
                list.add(value);
                return this;
            }

            @Override
            public Object build() {
                return List.copyOf(list);
            }
        };
    }

    @Override
    public @NotNull Result<MapLike<Object>> getMap(@NotNull Object value) {
        if (!(value instanceof Map<?, ?> map))
            return new Result.Error<>("Not a map: " + value);
        return new Result.Ok<>(new MapLike<>() {
            @Override
            public @NotNull Collection<String> keys() {
                if (map.isEmpty()) return List.of();
                var keys = List.copyOf(map.keySet());
                if (keys.getFirst() instanceof String)
                    //noinspection unchecked
                    return (List<String>) keys;
                return List.of(); // No string keys
            }

            @Override
            public boolean hasValue(@NotNull String key) {
                return map.containsKey(key);
            }

            @Override
            public @NotNull Result<Object> getValue(@NotNull String key) {
                if (!hasValue(key)) return new Result.Error<>("No such key: " + key);
                return new Result.Ok<>(map.get(key));
            }
        });
    }

    @Override
    public @NotNull MapBuilder<Object> createMap() {
        final Map<String, Object> map = new HashMap<>();
        return new MapBuilder<>() {
            @Override
            public @NotNull MapBuilder<Object> put(@NotNull Object key, Object value) {
                if (!(key instanceof String s)) return this;
                map.put(s, value);
                return this;
            }

            @Override
            public @NotNull MapBuilder<Object> put(@NotNull String key, Object value) {
                map.put(key, value);
                return this;
            }

            @Override
            public Object build() {
                return Map.copyOf(map);
            }
        };
    }

    @Override
    public @NotNull <O> Result<O> convertTo(@NotNull Transcoder<O> coder, @NotNull Object value) {
        return switch (value) {
            case Optional<?> o when o.isEmpty() -> new Result.Ok<>(coder.createNull());
            case Boolean b -> new Result.Ok<>(coder.createBoolean(b));
            case Byte n -> new Result.Ok<>(coder.createByte(n));
            case Short n -> new Result.Ok<>(coder.createShort(n));
            case Integer n -> new Result.Ok<>(coder.createInt(n));
            case Long n -> new Result.Ok<>(coder.createLong(n));
            case Float n -> new Result.Ok<>(coder.createFloat(n));
            case Double n -> new Result.Ok<>(coder.createDouble(n));
            case Number n -> new Result.Ok<>(coder.createDouble(n.doubleValue()));
            case String s -> new Result.Ok<>(coder.createString(s));
            case List<?> l -> {
                var builder = coder.createList(l.size());
                for (var o : l) {
                    var result = convertTo(coder, o);
                    if (!(result instanceof Result.Ok(O inner)))
                        yield result.cast();
                    builder.add(inner);
                }
                yield new Result.Ok<>(builder.build());
            }
            case Map<?, ?> m -> {
                var builder = coder.createMap();
                for (var entry : m.entrySet()) {
                    var key = entry.getKey();
                    if (!(key instanceof String s))
                        yield new Result.Error<>("Map key is not a string: " + key);
                    var result = convertTo(coder, entry.getValue());
                    if (!(result instanceof Result.Ok(O inner)))
                        yield result.cast();
                    builder.put(s, inner);
                }
                yield new Result.Ok<>(builder.build());
            }
            default -> new Result.Error<>("Unsupported type: " + value);
        };
    }
}
