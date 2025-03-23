package net.minestom.server.codec;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class TranscoderJsonImpl implements Transcoder<JsonElement> {
    public static final TranscoderJsonImpl INSTANCE = new TranscoderJsonImpl();

    @Override
    public @NotNull JsonElement createNull() {
        return JsonNull.INSTANCE;
    }

    @Override
    public @NotNull Result<Boolean> getBoolean(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive))
            return new Result.Error<>("Not a boolean: " + value);
        return new Result.Ok<>(primitive.getAsBoolean());
    }

    @Override
    public @NotNull JsonElement createBoolean(boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<Byte> getByte(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a byte: " + value);
        return new Result.Ok<>(primitive.getAsByte());
    }

    @Override
    public @NotNull JsonElement createByte(byte value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<Short> getShort(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a short: " + value);
        return new Result.Ok<>(primitive.getAsShort());
    }

    @Override
    public @NotNull JsonElement createShort(short value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<Integer> getInt(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not an int: " + value);
        return new Result.Ok<>(primitive.getAsInt());
    }

    @Override
    public @NotNull JsonElement createInt(int value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<Long> getLong(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a long: " + value);
        return new Result.Ok<>(primitive.getAsLong());
    }

    @Override
    public @NotNull JsonElement createLong(long value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<Float> getFloat(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a float: " + value);
        return new Result.Ok<>(primitive.getAsFloat());
    }

    @Override
    public @NotNull JsonElement createFloat(float value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<Double> getDouble(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a double: " + value);
        return new Result.Ok<>(primitive.getAsDouble());
    }

    @Override
    public @NotNull JsonElement createDouble(double value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<String> getString(@NotNull JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive))
            return new Result.Error<>("Not a string: " + value);
        return new Result.Ok<>(primitive.getAsString());
    }

    @Override
    public @NotNull JsonElement createString(@NotNull String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Result<List<JsonElement>> getList(@NotNull JsonElement value) {
        if (!(value instanceof JsonArray array)) return new Result.Error<>("Not a list: " + value);
        if (array.isEmpty()) return new Result.Ok<>(List.of());
        return new Result.Ok<>(new AbstractList<>() {
            @Override
            public JsonElement get(int index) {
                return array.get(index);
            }

            @Override
            public int size() {
                return array.size();
            }
        });
    }

    @Override
    public @NotNull JsonElement emptyList() {
        return new JsonArray();
    }

    @Override
    public @NotNull ListBuilder<JsonElement> createList(int expectedSize) {
        final JsonArray list = new JsonArray(expectedSize);
        return new ListBuilder<>() {
            @Override
            public @NotNull ListBuilder<JsonElement> add(JsonElement value) {
                list.add(value);
                return this;
            }

            @Override
            public JsonElement build() {
                return list;
            }
        };
    }

    @Override
    public @NotNull Result<MapLike<JsonElement>> getMap(@NotNull JsonElement value) {
        if (!(value instanceof JsonObject object))
            return new Result.Error<>("Not an object: " + value);
        return new Result.Ok<>(new MapLike<>() {
            @Override
            public @NotNull Collection<String> keys() {
                return object.keySet();
            }

            @Override
            public boolean hasValue(@NotNull String key) {
                return object.has(key);
            }

            @Override
            public @NotNull Result<JsonElement> getValue(@NotNull String key) {
                final JsonElement element = object.get(key);
                if (element == null) return new Result.Error<>("No such key: " + key);
                return new Result.Ok<>(element);
            }

            @Override
            public int size() {
                return object.size();
            }
        });
    }

    @Override
    public @NotNull JsonElement emptyMap() {
        return new JsonObject();
    }

    @Override
    public @NotNull MapBuilder<JsonElement> createMap() {
        final JsonObject object = new JsonObject();
        return new MapBuilder<>() {
            @Override
            public @NotNull MapBuilder<JsonElement> put(@NotNull JsonElement key, JsonElement value) {
                return put(key.getAsString(), value);
            }

            @Override
            public @NotNull MapBuilder<JsonElement> put(@NotNull String key, JsonElement value) {
                if (value != JsonNull.INSTANCE)
                    object.add(key, value);
                return this;
            }

            @Override
            public JsonElement build() {
                return object;
            }
        };
    }

    @Override
    public @NotNull <O> Result<O> convertTo(@NotNull Transcoder<O> coder, @NotNull JsonElement value) {
        return switch (value) {
            case JsonObject object -> {
                final MapBuilder<O> mapBuilder = coder.createMap();
                for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    final String key = entry.getKey();
                    switch (convertTo(coder, entry.getValue())) {
                        case Result.Ok(O data) -> mapBuilder.put(coder.createString(key), data);
                        case Result.Error(String message) -> {
                            yield new Result.Error<>(key + ": " + message);
                        }
                    }
                }
                yield new Result.Ok<>(mapBuilder.build());
            }
            case JsonArray array -> {
                // TODO(1.21.5) empty list call on coder
                final ListBuilder<O> listBuilder = coder.createList(array.size());
                for (int i = 0; i < array.size(); i++) {
                    switch (convertTo(coder, array.get(i))) {
                        case Result.Ok(O data) -> listBuilder.add(data);
                        case Result.Error(String message) -> {
                            yield new Result.Error<>(i + ": " + message);
                        }
                    }
                }
                yield new Result.Ok<>(listBuilder.build());
            }
            case JsonPrimitive primitive when primitive.isBoolean() ->
                    new Result.Ok<>(coder.createBoolean(primitive.getAsBoolean()));
            case JsonPrimitive primitive when primitive.isNumber() ->
                    new Result.Ok<>(coder.createDouble(primitive.getAsDouble()));
            case JsonPrimitive primitive when primitive.isString() ->
                    new Result.Ok<>(coder.createString(primitive.getAsString()));
            case JsonNull jsonNull -> new Result.Ok<>(coder.createNull());
            default -> new Result.Error<>("Unknown JSON type: " + value);
        };
    }
}
