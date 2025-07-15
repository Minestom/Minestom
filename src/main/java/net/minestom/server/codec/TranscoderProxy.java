package net.minestom.server.codec;


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
    static Transcoder<?> extractDelegate(Transcoder<?> transcoder) {
        if (transcoder instanceof TranscoderProxy<?> proxy)
            return extractDelegate(proxy.delegate());
        return transcoder;
    }

    Transcoder<D> delegate();

    @Override
    default D createNull() {
        return delegate().createNull();
    }

    @Override
    default Result<Boolean> getBoolean(D value) {
        return delegate().getBoolean(value);
    }

    @Override
    default D createBoolean(boolean value) {
        return delegate().createBoolean(value);
    }

    @Override
    default Result<Byte> getByte(D value) {
        return delegate().getByte(value);
    }

    @Override
    default D createByte(byte value) {
        return delegate().createByte(value);
    }

    @Override
    default Result<Short> getShort(D value) {
        return delegate().getShort(value);
    }

    @Override
    default D createShort(short value) {
        return delegate().createShort(value);
    }

    @Override
    default Result<Integer> getInt(D value) {
        return delegate().getInt(value);
    }

    @Override
    default D createInt(int value) {
        return delegate().createInt(value);
    }

    @Override
    default Result<Long> getLong(D value) {
        return delegate().getLong(value);
    }

    @Override
    default D createLong(long value) {
        return delegate().createLong(value);
    }

    @Override
    default Result<Float> getFloat(D value) {
        return delegate().getFloat(value);
    }

    @Override
    default D createFloat(float value) {
        return delegate().createFloat(value);
    }

    @Override
    default Result<Double> getDouble(D value) {
        return delegate().getDouble(value);
    }

    @Override
    default D createDouble(double value) {
        return delegate().createDouble(value);
    }

    @Override
    default Result<String> getString(D value) {
        return delegate().getString(value);
    }

    @Override
    default D createString(String value) {
        return delegate().createString(value);
    }

    @Override
    default D createByteArray(byte[] value) {
        return delegate().createByteArray(value);
    }

    @Override
    default Result<byte[]> getByteArray(D value) {
        return delegate().getByteArray(value);
    }

    @Override
    default D createIntArray(int[] value) {
        return delegate().createIntArray(value);
    }

    @Override
    default Result<int[]> getIntArray(D value) {
        return delegate().getIntArray(value);
    }

    @Override
    default D createLongArray(long[] value) {
        return delegate().createLongArray(value);
    }

    @Override
    default Result<long[]> getLongArray(D value) {
        return delegate().getLongArray(value);
    }

    @Override
    default Result<List<D>> getList(D value) {
        return delegate().getList(value);
    }

    @Override
    default Result<MapLike<D>> getMap(D value) {
        return delegate().getMap(value);
    }

    @Override
    default MapBuilder<D> createMap() {
        return delegate().createMap();
    }

    @Override
    default ListBuilder<D> createList(int expectedSize) {
        return delegate().createList(expectedSize);
    }

    @Override
    default <O> Result<O> convertTo(Transcoder<O> coder, D value) {
        return delegate().convertTo(coder, value);
    }
}
