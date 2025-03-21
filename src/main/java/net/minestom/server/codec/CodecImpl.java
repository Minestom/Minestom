package net.minestom.server.codec;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.UUID;
import java.util.function.Function;

final class CodecImpl {

    interface PrimitiveEncoder<T> {
        <D> @NotNull D encode(@NotNull Transcoder<D> coder, @NotNull T value);
    }

    @SuppressWarnings("unchecked")
    record PrimitiveImpl<T>(@NotNull PrimitiveEncoder<T> encoder, @NotNull Decoder<T> decoder) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return decoder.decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            if (value == null) return new Result.Error<>("null");
            return (Result<D>) encoder.encode(coder, value);
        }
    }

    record OptionalImpl<T>(@NotNull Codec<T> inner, @Nullable T defaultValue) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Ok<>(inner.decode(coder, value).orElse(defaultValue));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            if (value == null) return new Result.Ok<>(coder.createNull());
            return inner.encode(coder, value);
        }
    }

    record TransformImpl<T, S>(@NotNull Codec<T> inner, @NotNull Function<T, S> to,
                               @NotNull Function<S, T> from) implements Codec<S> {
        @Override
        public @NotNull <D> Result<S> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Ok<>(to.apply(inner.decode(coder, value).orElse(null)));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable S value) {
            return inner.encode(coder, from.apply(value));
        }
    }

    record ListImpl<T>(@NotNull Codec<T> inner, int maxSize) implements Codec<List<T>> {
        @Override
        public @NotNull <D> Result<List<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Integer> sizeResult = coder.listSize(value);
            if (!(sizeResult instanceof Result.Ok(Integer size)))
                return sizeResult.cast();
            if (size > maxSize) return new Result.Error<>("List size exceeds maximum allowed size: " + maxSize);
            final List<T> decodedList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                final Result<D> itemResult = coder.getIndex(value, i);
                if (!(itemResult instanceof Result.Ok(D item)))
                    return itemResult.cast();
                Result<T> decodedItem = inner.decode(coder, item);
                if (!(decodedItem instanceof Result.Ok(T valueItem)))
                    return decodedItem.cast();
                decodedList.add(valueItem);
            }
            return new Result.Ok<>(decodedList);
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable List<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                throw new IllegalArgumentException("List size exceeds maximum allowed size: " + maxSize);
            Transcoder.ListBuilder<D> encodedList = coder.createList(value.size());
            for (T item : value) {
                final Result<D> itemResult = inner.encode(coder, item);
                if (!(itemResult instanceof Result.Ok(D encodedItem)))
                    return itemResult.cast();
                encodedList.add(encodedItem);
            }
            return new Result.Ok<>(encodedList.build());
        }
    }

    record SetImpl<T>(@NotNull Codec<T> inner, int maxSize) implements Codec<Set<T>> {
        @Override
        public @NotNull <D> Result<Set<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Integer> sizeResult = coder.listSize(value);
            if (!(sizeResult instanceof Result.Ok(Integer size)))
                return sizeResult.cast();
            if (size > maxSize) return new Result.Error<>("List size exceeds maximum allowed size: " + maxSize);
            final List<T> decodedList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                final Result<D> itemResult = coder.getIndex(value, i);
                if (!(itemResult instanceof Result.Ok(D item)))
                    return itemResult.cast();
                Result<T> decodedItem = inner.decode(coder, item);
                if (!(decodedItem instanceof Result.Ok(T valueItem)))
                    return decodedItem.cast();
                decodedList.add(valueItem);
            }
            return new Result.Ok<>(Set.copyOf(decodedList));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Set<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                throw new IllegalArgumentException("List size exceeds maximum allowed size: " + maxSize);
            Transcoder.ListBuilder<D> encodedList = coder.createList(value.size());
            for (T item : value) {
                final Result<D> itemResult = inner.encode(coder, item);
                if (!(itemResult instanceof Result.Ok(D encodedItem)))
                    return itemResult.cast();
                encodedList.add(encodedItem);
            }
            return new Result.Ok<>(encodedList.build());
        }
    }

    record MapImpl<K, V>(@NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec, int maxSize) implements Codec<Map<K, V>> {
        @Override
        public @NotNull <D> Result<Map<K, V>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Collection<Map.Entry<String, D>>> entriesResult = coder.getMapEntries(value);
            if (!(entriesResult instanceof Result.Ok(Collection<Map.Entry<String, D>> entries)))
                return entriesResult.cast();
            if (entries.size() > maxSize) return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (entries.isEmpty()) return new Result.Ok<>(Map.of());

            final Map<K, V> decodedMap = new HashMap<>(entries.size());
            for (final Map.Entry<String, D> entry : entries) {
                final Result<D> keyDResult = coder.getValue(value, entry.getKey());
                if (!(keyDResult instanceof Result.Ok(D keyD)))
                    return keyDResult.cast();
                final Result<K> keyResult = keyCodec.decode(coder, keyD);
                if (!(keyResult instanceof Result.Ok(K decodedKey)))
                    return keyResult.cast();
                final Result<V> valueResult = valueCodec.decode(coder, entry.getValue());
                if (!(valueResult instanceof Result.Ok(V decodedValue)))
                    return valueResult.cast();
                decodedMap.put(decodedKey, decodedValue);
            }
            return new Result.Ok<>(decodedMap);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Map<K, V> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (value.isEmpty()) return new Result.Ok<>(coder.createMap().build());

            final Transcoder.MapBuilder<D> map = coder.createMap();
            for (final Map.Entry<K, V> entry : value.entrySet()) {
                final Result<D> keyResult = keyCodec.encode(coder, entry.getKey());
                if (!(keyResult instanceof Result.Ok(D encodedKey)))
                    return keyResult.cast();
                final Result<String> keyStringResult = coder.getString(encodedKey);
                if (!(keyStringResult instanceof Result.Ok(String keyString)))
                    return keyStringResult.cast();
                final Result<D> valueResult = valueCodec.encode(coder, entry.getValue());
                if (!(valueResult instanceof Result.Ok(D encodedValue)))
                    return valueResult.cast();
                map.put(keyString, encodedValue);
            }

            return new Result.Ok<>(map.build());
        }
    }

    record UnionImpl<T, R>(@NotNull String keyField, @NotNull Codec<T> keyCodec,
                           @NotNull Function<T, Codec<R>> serializers,
                           @NotNull Function<R, T> keyFunc) implements Codec<R> {
        @Override
        public @NotNull <D> Result<R> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<D> discriminantResult = coder.getValue(value, keyField);
            if (!(discriminantResult instanceof Result.Ok(D discriminant)))
                return discriminantResult.cast();
            if (discriminant == null) return new Result.Error<>("null");

            final Result<T> keyResult = keyCodec.decode(coder, discriminant);
            if (!(keyResult instanceof Result.Ok(T key)))
                return keyResult.cast();

            return serializers.apply(key).decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable R value) {
            if (value == null) return new Result.Error<>("null");

            final T key = keyFunc.apply(value);
            var serializer = serializers.apply(key);
            if (serializer == null) return new Result.Error<>("no union value: " + key);

            final Result<D> keyResult = keyCodec.encode(coder, key);
            if (!(keyResult instanceof Result.Ok(D keyValue)))
                return keyResult.cast();
            if (keyValue == null) return new Result.Error<>("null");

            final Result<D> serializedResult = serializer.encode(coder, value);
            if (!(serializedResult instanceof Result.Ok(D serializedValue)))
                return serializedResult.cast();
            if (serializedValue == null) return new Result.Error<>("null");

            return coder.putValue(serializedValue, keyField, keyValue);
        }
    }

    static final class RecursiveImpl<T> implements Codec<T> {
        private final Codec<T> delegate;

        public RecursiveImpl(@NotNull Function<Codec<T>, Codec<T>> self) {
            this.delegate = self.apply(this);
        }

        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return delegate.decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            return delegate.encode(coder, value);
        }
    }

    record OrElseImpl<T>(@NotNull Codec<T> primary, @NotNull Codec<T> secondary) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<T> primaryResult = primary.decode(coder, value);
            if (primaryResult instanceof Result.Ok<T> primaryOk)
                return primaryOk;

            // Primary did not work, try secondary
            final Result<T> secondaryResult = secondary.decode(coder, value);
            if (secondaryResult instanceof Result.Ok<T> secondaryOk)
                return secondaryOk;

            // Secondary did not work either, return error from primary.
            return primaryResult;
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            return primary.encode(coder, value);
        }
    }

    record UUIDImpl() implements Codec<UUID> {
        @Override
        public @NotNull <D> Result<UUID> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<int[]> uuidResult = coder.getIntArray(value);

            if (uuidResult instanceof Result.Ok<int[]>(int[] ints) && ints.length == 4) {
                return new Result.Ok<>(new UUID(
                        ((long) ints[0] << 32) | (ints[1] & 0xFFFFFFFFL),
                        ((long) ints[2] << 32) | (ints[3] & 0xFFFFFFFFL)
                ));
            }

            return new Result.Error<>("Invalid UUID value or length: " + value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable UUID value) {
            if (value == null) return new Result.Error<>("Cannot encode a null UUID");

            return new Result.Ok<>(coder.createIntArray(new int[]{
                    (int) (value.getMostSignificantBits() >>> 32), (int) value.getMostSignificantBits(),
                    (int) (value.getLeastSignificantBits() >>> 32), (int) value.getLeastSignificantBits()
            }));
        }
    }

    record ComponentImpl() implements Codec<Component> {
        @Override
        public @NotNull <D> Result<Component> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return null;
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Component value) {
            return null;
        }
    }

    record BlockPositionImpl() implements Codec<Point> {
        @Override
        public @NotNull <D> Result<Point> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<int[]> intArrayResult = coder.getIntArray(value);
            if (!(intArrayResult instanceof Result.Ok(int[] intArray)))
                return intArrayResult.cast();
            if (intArray.length != 3)
                return new Result.Error<>("Invalid length for Point, expected 3 but got " + intArray.length);
            return new Result.Ok<>(new Vec(intArray[0], intArray[1], intArray[2]));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Point value) {
            return new Result.Ok<>(coder.createIntArray(new int[]{
                    (int) value.x(),
                    (int) value.y(),
                    (int) value.z()
            }));
        }
    }

}
