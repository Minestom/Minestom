package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Proxies all transcoder calls to the given delegate. Exists to allow passing context into
 * codec implementations by creating {@link Transcoder} subclasses.
 *
 * <p>Note: TranscoderProxy has some special handling assuming that <i>all</i> calls are forwarded.
 * If that is not the case, you should inherit from Transcoder itself and NOT TranscoderProxy.</p>
 *
 * @param <D> the type of the delegate transcoder
 */
public interface TranscoderProxy<D> extends Transcoder<D> {
    /**
     * Recursively extracts the delegate from a {@link TranscoderProxy}.
     *
     * @param transcoder The transcoder (possibly proxy) to extract
     * @return The delegate transcoder
     */
    static @NotNull Transcoder<?> extractDelegate(@NotNull Transcoder<?> transcoder) {
        if (transcoder instanceof TranscoderProxy<?> proxy)
            return extractDelegate(proxy.delegate());
        return transcoder;
    }

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
    default @NotNull D createByteArray(byte[] value) {
        return delegate().createByteArray(value);
    }

    @Override
    default @NotNull Result<byte[]> getByteArray(@NotNull D value) {
        return delegate().getByteArray(value);
    }

    @Override
    default @NotNull D createIntArray(int[] value) {
        return delegate().createIntArray(value);
    }

    @Override
    default @NotNull Result<int[]> getIntArray(@NotNull D value) {
        return delegate().getIntArray(value);
    }

    @Override
    default @NotNull D createLongArray(long[] value) {
        return delegate().createLongArray(value);
    }

    @Override
    default @NotNull Result<long[]> getLongArray(@NotNull D value) {
        return delegate().getLongArray(value);
    }

    @Override
    default @NotNull Result<List<D>> getList(@NotNull D value) {
        return delegate().getList(value);
    }

    @Override
    default @NotNull Result<MapLike<D>> getMap(@NotNull D value) {
        return delegate().getMap(value);
    }

    @Override
    default @NotNull MapBuilder<D> createMap() {
        return delegate().createMap();
    }

    @Override
    default @NotNull ListBuilder<D> createList(int expectedSize) {
        return delegate().createList(expectedSize);
    }

    @Override
    default @NotNull <O> Result<O> convertTo(@NotNull Transcoder<O> coder, @NotNull D value) {
        return delegate().convertTo(coder, value);
    }
}
