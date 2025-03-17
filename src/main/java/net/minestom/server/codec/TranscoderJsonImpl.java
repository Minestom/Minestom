package net.minestom.server.codec;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        return new Result.Ok<>(array.asList());
    }

    @Override
    public @NotNull JsonElement createList(@NotNull List<JsonElement> value) {
        final JsonArray array = new JsonArray(value.size());
        for (JsonElement element : value) array.add(element);
        return array;
    }

    @Override
    public boolean hasValue(@NotNull JsonElement value, @NotNull String key) {
        if (!(value instanceof JsonObject object)) return false;
        return object.has(key);
    }

    @Override
    public @NotNull Result<JsonElement> getValue(@NotNull JsonElement value, @NotNull String key) {
        if (!(value instanceof JsonObject object))
            return new Result.Error<>("Not an object: " + value);
        final JsonElement element = object.get(key);
        if (element == null) return new Result.Error<>("No such key: " + key);
        return new Result.Ok<>(element);
    }

    @Override
    public @NotNull MapBuilder<JsonElement> createMap() {
        final JsonObject object = new JsonObject();
        return new MapBuilder<>() {
            @Override
            public void put(@NotNull String key, JsonElement value) {
                object.add(key, value);
            }

            @Override
            public JsonElement build() {
                return object;
            }
        };
    }
}
