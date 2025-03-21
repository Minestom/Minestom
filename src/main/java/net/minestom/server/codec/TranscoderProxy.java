package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TranscoderProxy<D> extends Transcoder<D> {

    @NotNull Transcoder<D> delegate();

    @Override
    default @NotNull D createNull() {
        return delegate().createNull();
    }

    @Override
    default @NotNull Result<Boolean> getBoolean(@NotNull D value) {
        return delegate().getBoolean(value);
    }

    @Override
    default @NotNull D createBoolean(boolean value) {
        return delegate().createBoolean(value);
    }

    @Override
    default @NotNull Result<Byte> getByte(@NotNull D value) {
        return delegate().getByte(value);
    }

    @Override
    default @NotNull D createByte(byte value) {
        return delegate().createByte(value);
    }

    @Override
    default @NotNull Result<Short> getShort(@NotNull D value) {
        return delegate().getShort(value);
    }

    @Override
    default @NotNull D createShort(short value) {
        return delegate().createShort(value);
    }

    @Override
    default @NotNull Result<Integer> getInt(@NotNull D value) {
        return delegate().getInt(value);
    }

    @Override
    default @NotNull D createInt(int value) {
        return delegate().createInt(value);
    }

    @Override
    default @NotNull Result<Long> getLong(@NotNull D value) {
        return delegate().getLong(value);
    }

    @Override
    default @NotNull D createLong(long value) {
        return delegate().createLong(value);
    }

    @Override
    default @NotNull Result<Float> getFloat(@NotNull D value) {
        return delegate().getFloat(value);
    }

    @Override
    default @NotNull D createFloat(float value) {
        return delegate().createFloat(value);
    }

    @Override
    default @NotNull Result<Double> getDouble(@NotNull D value) {
        return delegate().getDouble(value);
    }

    @Override
    default @NotNull D createDouble(double value) {
        return delegate().createDouble(value);
    }

    @Override
    default @NotNull Result<String> getString(@NotNull D value) {
        return delegate().getString(value);
    }

    @Override
    default @NotNull D createString(@NotNull String value) {
        return delegate().createString(value);
    }

    @Override
    default @NotNull Result<List<D>> getList(@NotNull D value) {
        return delegate().getList(value);
    }

    @Override
    default @NotNull D createList(@NotNull List<D> value) {
        return delegate().createList(value);
    }

    @Override
    default boolean hasValue(@NotNull D value, @NotNull String key) {
        return delegate().hasValue(value, key);
    }

    @Override
    default @NotNull Result<D> getValue(@NotNull D value, @NotNull String key) {
        return delegate().getValue(value, key);
    }

    @Override
    default @NotNull MapBuilder<D> createMap() {
        return delegate().createMap();
    }

    @Override
    default @NotNull Result<Integer> listSize(@NotNull D value) {
        return delegate().listSize(value);
    }

    @Override
    default @NotNull Result<D> getIndex(@NotNull D value, int index) {
        return delegate().getIndex(value, index);
    }

    @Override
    default @NotNull ListBuilder<D> createList(int expectedSize) {
        return delegate().createList(expectedSize);
    }

    @Override
    default @NotNull Result<D> putValue(@NotNull D map, @NotNull String key, @NotNull D value) {
        return delegate().putValue(map, key, value);
    }
}
