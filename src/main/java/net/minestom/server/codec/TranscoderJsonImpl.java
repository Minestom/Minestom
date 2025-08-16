package net.minestom.server.codec;

import com.google.gson.*;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class TranscoderJsonImpl implements Transcoder<JsonElement> {
    public static final TranscoderJsonImpl INSTANCE = new TranscoderJsonImpl();

    @Override
    public JsonElement createNull() {
        return JsonNull.INSTANCE;
    }

    @Override
    public Result<Boolean> getBoolean(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive))
            return new Result.Error<>("Not a boolean: " + value);
        return new Result.Ok<>(primitive.getAsBoolean());
    }

    @Override
    public JsonElement createBoolean(boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<Byte> getByte(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a byte: " + value);
        return new Result.Ok<>(primitive.getAsByte());
    }

    @Override
    public JsonElement createByte(byte value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<Short> getShort(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a short: " + value);
        return new Result.Ok<>(primitive.getAsShort());
    }

    @Override
    public JsonElement createShort(short value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<Integer> getInt(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not an int: " + value);
        return new Result.Ok<>(primitive.getAsInt());
    }

    @Override
    public JsonElement createInt(int value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<Long> getLong(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a long: " + value);
        return new Result.Ok<>(primitive.getAsLong());
    }

    @Override
    public JsonElement createLong(long value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<Float> getFloat(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a float: " + value);
        return new Result.Ok<>(primitive.getAsFloat());
    }

    @Override
    public JsonElement createFloat(float value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<Double> getDouble(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive) || !primitive.isNumber())
            return new Result.Error<>("Not a double: " + value);
        return new Result.Ok<>(primitive.getAsDouble());
    }

    @Override
    public JsonElement createDouble(double value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<String> getString(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive))
            return new Result.Error<>("Not a string: " + value);
        return new Result.Ok<>(primitive.getAsString());
    }

    @Override
    public JsonElement createString(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Result<List<JsonElement>> getList(JsonElement value) {
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
    public JsonElement emptyList() {
        return new JsonArray();
    }

    @Override
    public ListBuilder<JsonElement> createList(int expectedSize) {
        final JsonArray list = new JsonArray(expectedSize);
        return new ListBuilder<>() {
            @Override
            public ListBuilder<JsonElement> add(JsonElement value) {
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
    public Result<MapLike<JsonElement>> getMap(JsonElement value) {
        if (!(value instanceof JsonObject object))
            return new Result.Error<>("Not an object: " + value);
        return new Result.Ok<>(new MapLike<>() {
            @Override
            public Collection<String> keys() {
                return object.keySet();
            }

            @Override
            public boolean hasValue(String key) {
                return object.has(key);
            }

            @Override
            public Result<JsonElement> getValue(String key) {
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
    public JsonElement emptyMap() {
        return new JsonObject();
    }

    @Override
    public MapBuilder<JsonElement> createMap() {
        final JsonObject object = new JsonObject();
        return new MapBuilder<>() {
            @Override
            public MapBuilder<JsonElement> put(JsonElement key, JsonElement value) {
                return put(key.getAsString(), value);
            }

            @Override
            public MapBuilder<JsonElement> put(String key, JsonElement value) {
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
    public <O> Result<O> convertTo(Transcoder<O> coder, JsonElement value) {
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
                if (array.isEmpty()) yield new Result.Ok<>(coder.emptyList());
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
