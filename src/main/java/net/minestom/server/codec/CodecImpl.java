package net.minestom.server.codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Transcoder.MapLike;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

final class CodecImpl {

    record RawValueImpl<D>(@NotNull Transcoder<D> coder, @NotNull D value) implements Codec.RawValue {

        RawValueImpl {
            Objects.requireNonNull(coder);
            Objects.requireNonNull(value);
        }

        @Override
        public @NotNull <D1> Result<D1> convertTo(@NotNull Transcoder<D1> coder) {
            // If the two transcoders are the same instance, we can immediately return the value.
            if (TranscoderProxy.extractDelegate(this.coder) == TranscoderProxy.extractDelegate(coder))
                //noinspection unchecked
                return new Result.Ok<>((D1) value);
            return this.coder.convertTo(coder, value);
        }
    }

    record RawValueCodecImpl() implements Codec<Codec.RawValue> {
        @Override
        public @NotNull <D> Result<RawValue> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Ok<>(new RawValueImpl<>(coder, value));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable RawValue value) {
            if (value == null) return new Result.Error<>("null");
            return value.convertTo(coder);
        }
    }

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
            return new Result.Ok<>(encoder.encode(coder, value));
        }
    }

    record OptionalImpl<T>(@NotNull Codec<T> inner, @Nullable T defaultValue) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Ok<>(inner.decode(coder, value).orElse(defaultValue));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            if (value == null || Objects.equals(value, defaultValue))
                return new Result.Ok<>(coder.createNull());
            return inner.encode(coder, value);
        }
    }

    record TransformImpl<T, S>(@NotNull Codec<T> inner, @NotNull Function<T, S> to,
                               @NotNull Function<S, T> from) implements Codec<S> {
        @Override
        public @NotNull <D> Result<S> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            try {
                final Result<T> innerResult = inner.decode(coder, value);
                return switch (innerResult) {
                    case Result.Ok(T inner) -> new Result.Ok<>(to.apply(inner));
                    case Result.Error(String error) -> new Result.Error<>(error);
                };
            } catch (Exception e) {
                return new Result.Error<>(e.getMessage());
            }
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable S value) {
            try {
                return inner.encode(coder, from.apply(value));
            } catch (Exception e) {
                return new Result.Error<>(e.getMessage());
            }
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
            return new Result.Ok<>(List.copyOf(decodedList));
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
                if (encodedItem != null)
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

    record MapImpl<K, V>(@NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec,
                         int maxSize) implements Codec<Map<K, V>> {
        @Override
        public @NotNull <D> Result<Map<K, V>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Collection<Map.Entry<String, D>>> entriesResult = coder.getMapEntries(value);
            if (!(entriesResult instanceof Result.Ok(Collection<Map.Entry<String, D>> entries)))
                return entriesResult.cast();
            if (entries.size() > maxSize)
                return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (entries.isEmpty()) return new Result.Ok<>(Map.of());

            final Map<K, V> decodedMap = new HashMap<>(entries.size());
            for (final Map.Entry<String, D> entry : entries) {
                final Result<K> keyResult = keyCodec.decode(coder, coder.createString(entry.getKey()));
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
                           @NotNull Function<T, StructCodec<R>> serializers,
                           @NotNull Function<R, T> keyFunc) implements StructCodec<R> {

        @Override
        public @NotNull <D> Result<R> decodeFromMap(@NotNull Transcoder<D> coder, @NotNull MapLike<D> map) {
            final Result<T> keyResult = map.getValue(keyField).map(key -> keyCodec.decode(coder, key));
            if (!(keyResult instanceof Result.Ok(T key)))
                return keyResult.cast();
            return serializers.apply(key).decodeFromMap(coder, map);
        }

        @Override
        public @NotNull <D> Result<D> encodeToMap(@NotNull Transcoder<D> coder, @NotNull R value, Transcoder.@NotNull MapBuilder<D> map) {
            final T key = keyFunc.apply(value);
            var serializer = serializers.apply(key);
            if (serializer == null) return new Result.Error<>("no union value: " + key);

            final Result<D> keyResult = keyCodec.encode(coder, key);
            if (!(keyResult instanceof Result.Ok(D keyValue)))
                return keyResult.cast();
            if (keyValue == null) return new Result.Error<>("null");

            map.put(keyField, keyValue);
            return serializer.encodeToMap(coder, value, map);
        }
    }

    record RegistryKeyImpl<T>(@NotNull Registries.Selector<T> selector) implements Codec<DynamicRegistry.Key<T>> {
        @Override
        public @NotNull <D> Result<DynamicRegistry.Key<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = selector.select(context.registries());

            final Result<String> keyResult = coder.getString(value);
            if (!(keyResult instanceof Result.Ok(@KeyPattern String keyStr)))
                return keyResult.cast();
            final DynamicRegistry.Key<T> key = DynamicRegistry.Key.of(Key.key(keyStr));
            if (registry.getId(key) == -1)
                return new Result.Error<>("no registry value: " + key);
            return new Result.Ok<>(key);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, DynamicRegistry.@Nullable Key<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = selector.select(context.registries());

            if (registry.getId(value) == -1)
                return new Result.Error<>("no registry value: " + value);
            return new Result.Ok<>(coder.createString(value.name()));
        }
    }

    @SuppressWarnings("unchecked")
    record RegistryTaggedUnionImpl<T>(
            @NotNull Registries.Selector<StructCodec<? extends T>> registrySelector,
            @NotNull Function<T, StructCodec<? extends T>> valueToCodec,
            @NotNull String key
    ) implements StructCodec<T> {
        @Override
        public @NotNull <D> Result<T> decodeFromMap(@NotNull Transcoder<D> coder, @NotNull MapLike<D> map) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = registrySelector.select(context.registries());

            final Result<String> type = map.getValue(key).map(coder::getString);
            if (!(type instanceof Result.Ok(@KeyPattern String tag)))
                return type.mapError(e -> key + ": " + e).cast();
            final StructCodec<T> innerCodec = (StructCodec<T>) registry.get(Key.key(tag));
            if (innerCodec == null) return new Result.Error<>("No such key: " + tag);

            return innerCodec.decodeFromMap(coder, map);
        }

        @Override
        public @NotNull <D> Result<D> encodeToMap(@NotNull Transcoder<D> coder, @NotNull T value, Transcoder.@NotNull MapBuilder<D> map) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = registrySelector.select(context.registries());

            //noinspection unchecked
            final StructCodec<T> innerCodec = (StructCodec<T>) valueToCodec.apply(value);
            final DynamicRegistry.Key<StructCodec<? extends T>> type = registry.getKey(innerCodec);
            if (type == null) return new Result.Error<>("Unregistered serializer for: " + value);
            if (context.forClient() && registry.getPack(type) != DataPack.MINECRAFT_CORE)
                return new Result.Ok<>(null);

            map.put(key, coder.createString(type.name()));
            return innerCodec.encodeToMap(coder, value, map);
        }
    }

    static final class RecursiveImpl<T> implements Codec<T> {
        final Codec<T> delegate;

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

    static final class ForwardRefImpl<T> implements Codec<T> {
        private final Supplier<Codec<T>> delegateFunc;
        private Codec<T> delegate;

        ForwardRefImpl(Supplier<Codec<T>> delegateFunc) {
            this.delegateFunc = delegateFunc;
        }

        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            if (delegate == null) delegate = delegateFunc.get();
            return delegate.decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            if (delegate == null) delegate = delegateFunc.get();
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
            final Result<D> primaryResult = primary.encode(coder, value);
            if (primaryResult instanceof Result.Ok<D> primaryOk)
                return primaryOk;

            // Primary did not work, try secondary
            final Result<D> secondaryResult = secondary.encode(coder, value);
            if (secondaryResult instanceof Result.Ok<D> secondaryOk)
                return secondaryOk;

            // Secondary did not work either, return error from primary.
            return primaryResult;
        }
    }

    record Vector3DImpl() implements Codec<Point> {

        @Override
        public @NotNull <D> Result<Point> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<double[]> doubleArrayResult = coder.getDoubleArray(value);
            if (!(doubleArrayResult instanceof Result.Ok(double[] doubleArray)))
                return doubleArrayResult.cast();
            if (doubleArray.length != 3)
                return new Result.Error<>("Invalid length for Point, expected 3 but got " + doubleArray.length);
            return new Result.Ok<>(new Vec(doubleArray[0], doubleArray[1], doubleArray[2]));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Point value) {
            return new Result.Ok<>(coder.createDoubleArray(new double[]{
                    value.x(),
                    value.y(),
                    value.z()
            }));
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
            if (value == null) return new Result.Error<>("null");
            return new Result.Ok<>(coder.createIntArray(new int[]{
                    (int) value.x(),
                    (int) value.y(),
                    (int) value.z()
            }));
        }
    }

    /**
     * @deprecated Remove once adventure is updated to have change_page be an int.
     */
    @Deprecated
    record IntAsStringImpl() implements Codec<String> {
        @Override
        public @NotNull <D> Result<String> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return coder.getInt(value).mapResult(String::valueOf);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable String value) {
            if (value == null) return new Result.Error<>("null");
            try {
                return new Result.Ok<>(coder.createInt(Integer.parseInt(value)));
            } catch (NumberFormatException ignored) {
                return new Result.Error<>("not an integer: " + value);
            }
        }
    }

}
