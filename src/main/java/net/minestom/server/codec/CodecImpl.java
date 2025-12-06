package net.minestom.server.codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.util.TriState;
import net.minestom.server.codec.Transcoder.ListBuilder;
import net.minestom.server.codec.Transcoder.MapBuilder;
import net.minestom.server.codec.Transcoder.MapLike;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.ThrowingFunction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
final class CodecImpl {

    record RawValueImpl<D>(Transcoder<D> coder, D value) implements Codec.RawValue {
        RawValueImpl {
            Objects.requireNonNull(coder, "coder");
            Objects.requireNonNull(value, "value");
        }

        @Override
        public <D1> Result<D1> convertTo(Transcoder<D1> coder) {
            // If the two transcoders are the same instance, we can immediately return the value.
            if (TranscoderProxy.extractDelegate(this.coder) == TranscoderProxy.extractDelegate(coder))
                //noinspection unchecked
                return new Result.Ok<>((D1) value);
            return this.coder.convertTo(coder, value);
        }
    }

    record RawValueCodecImpl() implements Codec<Codec.RawValue> {
        @Override
        public <D> Result<RawValue> decode(Transcoder<D> coder, D value) {
            return new Result.Ok<>(new RawValueImpl<>(coder, value));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable RawValue value) {
            if (value == null) return new Result.Error<>("null");
            return value.convertTo(coder);
        }
    }

    @FunctionalInterface
    interface PrimitiveEncoder<T> {
        <D> D encode(Transcoder<D> coder, T value);
    }

    record PrimitiveImpl<T>(PrimitiveEncoder<T> encoder, Decoder<T> decoder) implements Codec<T> {
        PrimitiveImpl {
            Objects.requireNonNull(encoder, "encoder");
            Objects.requireNonNull(decoder, "decoder");
        }

        @Override
        public <D> Result<T> decode(Transcoder<D> coder, D value) {
            return decoder.decode(coder, value);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
            if (value == null) return new Result.Error<>("null");
            return new Result.Ok<>(encoder.encode(coder, value));
        }
    }

    record TriStateImpl() implements Codec<TriState> {
        @Override
        public <D> Result<TriState> decode(Transcoder<D> coder, D value) {
            final Result<Boolean> boolResult = coder.getBoolean(value);
            if (boolResult instanceof Result.Ok(Boolean bool))
                return new Result.Ok<>(TriState.byBoolean(bool));
            final Result<String> stringResult = coder.getString(value);
            if (stringResult instanceof Result.Ok(String string)) {
                if ("true".equalsIgnoreCase(string)) return new Result.Ok<>(TriState.TRUE);
                if ("false".equalsIgnoreCase(string)) return new Result.Ok<>(TriState.FALSE);
                if ("default".equalsIgnoreCase(string)) return new Result.Ok<>(TriState.NOT_SET);
            }
            return new Result.Error<>("expected true, false, or \"default\", got: " + stringResult);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable TriState value) {
            return switch (value) {
                case TRUE -> new Result.Ok<>(coder.createBoolean(true));
                case FALSE -> new Result.Ok<>(coder.createBoolean(false));
                case null, default -> new Result.Ok<>(coder.createString("default"));
            };
        }
    }

    record OptionalImpl<T>(Codec<T> inner, @Nullable T defaultValue) implements Codec<T> {
        OptionalImpl {
            Objects.requireNonNull(inner, "inner");
        }

        @Override
        public <D> Result<T> decode(Transcoder<D> coder, D value) {
            return new Result.Ok<>(inner.decode(coder, value).orElse(defaultValue));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
            if (value == null || Objects.equals(value, defaultValue))
                return new Result.Ok<>(coder.createNull());
            return inner.encode(coder, value);
        }
    }

    record TransformImpl<T, S>(
            Codec<T> inner, ThrowingFunction<T, S> to,
            ThrowingFunction<@Nullable S, T> from
    ) implements Codec<S> {
        TransformImpl {
            Objects.requireNonNull(inner, "inner");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(from, "from");
        }

        @Override
        public <D> Result<S> decode(Transcoder<D> coder, D value) {
            try {
                final Result<T> innerResult = inner.decode(coder, value);
                return switch (innerResult) {
                    case Result.Ok(T innerValue) -> new Result.Ok<>(to.apply(innerValue));
                    case Result.Error(String error) -> new Result.Error<>(error);
                };
            } catch (Exception e) {
                return new Result.Error<>(e.getMessage());
            }
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable S value) {
            try {
                return inner.encode(coder, from.apply(value));
            } catch (Exception e) {
                return new Result.Error<>(e.getMessage());
            }
        }
    }

    record ListImpl<T>(Codec<T> inner, int maxSize) implements Codec<@Unmodifiable List<T>> {
        ListImpl {
            Objects.requireNonNull(inner, "inner");
        }

        @Override
        public <D> Result<@Unmodifiable List<T>> decode(Transcoder<D> coder, D value) {
            final Result<List<D>> listResult = coder.getList(value);
            if (!(listResult instanceof Result.Ok(List<D> list)))
                return listResult.cast();
            if (list.size() > maxSize)
                return new Result.Error<>("List size exceeds maximum allowed size: " + maxSize);

            final List<T> decodedList = new ArrayList<>(list.size());
            for (final D item : list) {
                Result<T> decodedItem = inner.decode(coder, item);
                if (!(decodedItem instanceof Result.Ok(T valueItem)))
                    return decodedItem.cast();
                decodedList.add(valueItem);
            }
            return new Result.Ok<>(List.copyOf(decodedList));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable List<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                throw new IllegalArgumentException("List size exceeds maximum allowed size: " + maxSize);
            final ListBuilder<D> encodedList = coder.createList(value.size());
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

    record SetImpl<T>(Codec<T> inner, int maxSize) implements Codec<@Unmodifiable Set<T>> {
        SetImpl {
            Objects.requireNonNull(inner, "inner");
        }

        @Override
        public <D> Result<@Unmodifiable Set<T>> decode(Transcoder<D> coder, D value) {
            final Result<List<D>> listResult = coder.getList(value);
            if (!(listResult instanceof Result.Ok(List<D> list)))
                return listResult.cast();
            if (list.size() > maxSize)
                return new Result.Error<>("Set size exceeds maximum allowed size: " + maxSize);

            final Set<T> decodedSet = new HashSet<>(list.size());
            for (final D item : list) {
                Result<T> decodedItem = inner.decode(coder, item);
                if (!(decodedItem instanceof Result.Ok(T valueItem)))
                    return decodedItem.cast();
                decodedSet.add(valueItem);
            }
            return new Result.Ok<>(Set.copyOf(decodedSet));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Set<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                throw new IllegalArgumentException("List size exceeds maximum allowed size: " + maxSize);
            ListBuilder<D> encodedList = coder.createList(value.size());
            for (T item : value) {
                final Result<D> itemResult = inner.encode(coder, item);
                if (!(itemResult instanceof Result.Ok(D encodedItem)))
                    return itemResult.cast();
                encodedList.add(encodedItem);
            }
            return new Result.Ok<>(encodedList.build());
        }
    }

    record MapImpl<K, V>(
            Codec<K> keyCodec, Codec<V> valueCodec,
            int maxSize
    ) implements Codec<@Unmodifiable Map<K, V>> {
        MapImpl {
            Objects.requireNonNull(keyCodec, "keyCodec");
            Objects.requireNonNull(valueCodec, "valueCodec");
        }

        @Override
        public <D> Result<@Unmodifiable Map<K, V>> decode(Transcoder<D> coder, D value) {
            final Result<MapLike<D>> mapResult = coder.getMap(value);
            if (!(mapResult instanceof Result.Ok(MapLike<D> map)))
                return mapResult.cast();

            if (map.size() > maxSize)
                return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (map.isEmpty()) return new Result.Ok<>(Map.of());

            final Map<K, V> decodedMap = new HashMap<>(map.size());
            for (final String key : map.keys()) {
                final Result<K> keyResult = keyCodec.decode(coder, coder.createString(key));
                if (!(keyResult instanceof Result.Ok(K decodedKey)))
                    return keyResult.cast();
                // The throwing decode here is fine since we are already iterating over known keys.
                final Result<V> valueResult = valueCodec.decode(coder, map.getValue(key).orElseThrow());
                if (!(valueResult instanceof Result.Ok(V decodedValue)))
                    return valueResult.cast();
                decodedMap.put(decodedKey, decodedValue);
            }
            return new Result.Ok<>(Map.copyOf(decodedMap));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Map<K, V> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (value.isEmpty()) return new Result.Ok<>(coder.createMap().build());

            final MapBuilder<D> map = coder.createMap();
            for (final Map.Entry<K, V> entry : value.entrySet()) {
                final Result<D> keyResult = keyCodec.encode(coder, entry.getKey());
                if (!(keyResult instanceof Result.Ok(D encodedKey)))
                    return keyResult.cast();
                final Result<D> valueResult = valueCodec.encode(coder, entry.getValue());
                if (!(valueResult instanceof Result.Ok(D encodedValue)))
                    return valueResult.cast();
                map.put(encodedKey, encodedValue);
            }

            return new Result.Ok<>(map.build());
        }
    }

    record TypedMapImpl<K, V>(
            Codec<K> keyCodec,
            Function<K, Codec<V>> valueMapper,
            int maxSize,
            @Nullable Map<K, Codec<V>> mutableResolvedValueCache
    ) implements Codec<@Unmodifiable Map<K, V>> {
        TypedMapImpl {
            Objects.requireNonNull(keyCodec, "keyCodec");
            Objects.requireNonNull(valueMapper, "valueMapper");
        }

        @Override
        public <D> Result<@Unmodifiable Map<K, V>> decode(Transcoder<D> coder, D value) {
            final Result<MapLike<D>> mapResult = coder.getMap(value);
            if (!(mapResult instanceof Result.Ok(MapLike<D> map)))
                return mapResult.cast();

            if (map.size() > maxSize)
                return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (map.isEmpty()) return new Result.Ok<>(Map.of());

            final Map<K, V> decodedMap = new HashMap<>(map.size());
            for (final String key : map.keys()) {
                final Result<K> keyResult = keyCodec.decode(coder, coder.createString(key));
                if (!(keyResult instanceof Result.Ok(K decodedKey)))
                    return keyResult.cast();

                final Codec<V> valueCodec = mutableResolvedValueCache != null
                        ? mutableResolvedValueCache.computeIfAbsent(decodedKey, valueMapper)
                        : valueMapper.apply(decodedKey);

                // The throwing decode here is fine since we are already iterating over known keys.
                final Result<V> valueResult = valueCodec.decode(coder, map.getValue(key).orElseThrow());
                if (!(valueResult instanceof Result.Ok(V decodedValue)))
                    return valueResult.cast();
                decodedMap.put(decodedKey, decodedValue);
            }
            return new Result.Ok<>(Map.copyOf(decodedMap));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Map<K, V> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                return new Result.Error<>("Map size exceeds maximum allowed size: " + maxSize);
            if (value.isEmpty()) return new Result.Ok<>(coder.createMap().build());

            final MapBuilder<D> map = coder.createMap();
            for (final Map.Entry<K, V> entry : value.entrySet()) {
                final Result<D> keyResult = keyCodec.encode(coder, entry.getKey());
                if (!(keyResult instanceof Result.Ok(D encodedKey)))
                    return keyResult.cast();

                final Codec<V> valueCodec = mutableResolvedValueCache != null
                        ? mutableResolvedValueCache.computeIfAbsent(entry.getKey(), valueMapper)
                        : valueMapper.apply(entry.getKey());

                final Result<D> valueResult = valueCodec.encode(coder, entry.getValue());
                if (!(valueResult instanceof Result.Ok(D encodedValue)))
                    return valueResult.cast();
                map.put(encodedKey, encodedValue);
            }

            return new Result.Ok<>(map.build());
        }
    }

    record UnionImpl<T, R>(
            String keyField, Codec<T> keyCodec,
            Function<T, @Nullable StructCodec<? extends R>> serializers,
            Function<R, ? extends T> keyFunc
    ) implements StructCodec<R> {
        UnionImpl {
            Objects.requireNonNull(serializers, "serializers");
            Objects.requireNonNull(keyField, "keyField");
            Objects.requireNonNull(keyFunc, "keyFunc");
        }

        @SuppressWarnings("unchecked")
        @Override
        public <D> Result<R> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
            final Result<T> keyResult = map.getValue(keyField).map(key -> keyCodec.decode(coder, key));
            if (!(keyResult instanceof Result.Ok(T key)))
                return keyResult.cast();
            final StructCodec<? extends R> serializer = serializers.apply(key);
            if (serializer == null) return new Result.Error<>("no union value: " + key);
            return (Result<R>) serializer.decodeFromMap(coder, map);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <D> Result<D> encodeToMap(Transcoder<D> coder, R value, MapBuilder<D> map) {
            final T key = keyFunc.apply(value);
            final StructCodec<R> serializer = (StructCodec<R>) serializers.apply(key);
            if (serializer == null) return new Result.Error<>("no union value: " + key);

            final Result<D> keyResult = keyCodec.encode(coder, key);
            if (!(keyResult instanceof Result.Ok(D keyValue)))
                return keyResult.cast();
            if (keyValue == null) return new Result.Error<>("null");

            map.put(keyField, keyValue);
            return serializer.encodeToMap(coder, value, map);
        }
    }

    @SuppressWarnings("unchecked")
    record RegistryTaggedUnionImpl<T>(
            String key,
            Registries.Selector<StructCodec<? extends T>> registrySelector,
            Function<T, StructCodec<? extends T>> valueToCodec
    ) implements StructCodec<T> {
        RegistryTaggedUnionImpl {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(registrySelector, "registrySelector");
            Objects.requireNonNull(valueToCodec, "valueToCodec");
        }

        @Override
        public <D> Result<T> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
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
        public <D> Result<@Nullable D> encodeToMap(Transcoder<D> coder, T value, MapBuilder<D> map) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = registrySelector.select(context.registries());

            //noinspection unchecked
            final StructCodec<T> innerCodec = (StructCodec<T>) valueToCodec.apply(value);
            final RegistryKey<StructCodec<? extends T>> type = registry.getKey(innerCodec);
            if (type == null) return new Result.Error<>("Unregistered serializer for: " + value);
            if (context.forClient() && registry.getPack(type) != DataPack.MINECRAFT_CORE)
                return new Result.Ok<>(null);

            map.put(key, coder.createString(type.key().asString()));
            return innerCodec.encodeToMap(coder, value, map);
        }
    }

    static final class RecursiveImpl<T> implements Codec<T> {
        final Codec<T> delegate;

        public RecursiveImpl(Function<Codec<T>, Codec<T>> self) {
            Objects.requireNonNull(self, "self");
            this.delegate = Objects.requireNonNull(self.apply(this), "delegate");
        }

        @Override
        public <D> Result<T> decode(Transcoder<D> coder, D value) {
            return delegate.decode(coder, value);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
            return delegate.encode(coder, value);
        }
    }

    static final class ForwardRefImpl<T> implements Codec<T> {
        private final Supplier<Codec<T>> delegateFunc;
        private @Nullable Codec<T> delegate;

        ForwardRefImpl(Supplier<Codec<T>> delegateFunc) {
            this.delegateFunc = Objects.requireNonNull(delegateFunc, "delegateFunc");
        }

        private Codec<T> delegate() {
            if (delegate == null) delegate = delegateFunc.get();
            return Objects.requireNonNull(delegate, "Delegate cannot be null after supplier call.");
        }

        @Override
        public <D> Result<T> decode(Transcoder<D> coder, D value) {
            return delegate().decode(coder, value);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
            return delegate().encode(coder, value);
        }
    }

    record OrElseImpl<T>(Codec<T> primary, Codec<T> secondary) implements Codec<T> {
        OrElseImpl {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
        }

        @Override
        public <D> Result<T> decode(Transcoder<D> coder, D value) {
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
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
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

    record BlockPositionImpl() implements Codec<Point> {
        @Override
        public <D> Result<Point> decode(Transcoder<D> coder, D value) {
            final Result<int[]> intArrayResult = coder.getIntArray(value);
            if (!(intArrayResult instanceof Result.Ok(int[] intArray)))
                return intArrayResult.cast();
            if (intArray.length != 3)
                return new Result.Error<>("Invalid length for Point, expected 3 but got " + intArray.length);
            return new Result.Ok<>(new Vec(intArray[0], intArray[1], intArray[2]));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Point value) {
            if (value == null) return new Result.Error<>("null");
            return new Result.Ok<>(coder.createIntArray(new int[]{
                    (int) value.x(),
                    (int) value.y(),
                    (int) value.z()
            }));
        }
    }

    record EitherImpl<L, R>(Codec<L> leftCodec, Codec<R> rightCodec) implements Codec<Either<L, R>> {
        EitherImpl {
            Objects.requireNonNull(leftCodec, "leftCodec");
            Objects.requireNonNull(rightCodec, "rightCodec");
        }

        @Override
        public <D> Result<Either<L, R>> decode(Transcoder<D> coder, D value) {
            final Result<L> leftResult = leftCodec.decode(coder, value);
            if (leftResult instanceof Result.Ok(L leftValue))
                return new Result.Ok<>(Either.left(leftValue));
            final Result<R> rightResult = rightCodec.decode(coder, value);
            if (rightResult instanceof Result.Ok(R rightValue))
                return new Result.Ok<>(Either.right(rightValue));
            return new Result.Error<>("Failed to decode Either: " + leftResult + ", " + rightResult);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Either<L, R> value) {
            if (value == null) return new Result.Error<>("null");
            return switch (value) {
                case Either.Left(L leftValue) -> leftCodec.encode(coder, leftValue);
                case Either.Right(R rightValue) -> rightCodec.encode(coder, rightValue);
            };
        }
    }

    record EitherStructImpl<L, R>(
            StructCodec<L> leftCodec,
            StructCodec<R> rightCodec
    ) implements StructCodec<Either<L, R>> {
        public EitherStructImpl {
            Objects.requireNonNull(leftCodec, "leftCodec");
            Objects.requireNonNull(rightCodec, "rightCodec");
        }

        @Override
        public <D> Result<Either<L, R>> decodeFromMap(Transcoder<D> coder, MapLike<D> map) {
            final Result<L> leftResult = leftCodec.decodeFromMap(coder, map);
            if (leftResult instanceof Result.Ok(L leftValue))
                return new Result.Ok<>(Either.left(leftValue));
            final Result<R> rightResult = rightCodec.decodeFromMap(coder, map);
            if (rightResult instanceof Result.Ok(R rightValue))
                return new Result.Ok<>(Either.right(rightValue));
            return new Result.Error<>("Failed to decode Either: " + leftResult + ", " + rightResult);
        }

        @Override
        public <D> Result<D> encodeToMap(Transcoder<D> coder, Either<L, R> value, MapBuilder<D> map) {
            return switch (value) {
                case Either.Left(L leftValue) -> leftCodec.encodeToMap(coder, leftValue, map);
                case Either.Right(R rightValue) -> rightCodec.encodeToMap(coder, rightValue, map);
            };
        }
    }

    record Vector3DImpl() implements Codec<Point> {
        @Override
        public <D> Result<Point> decode(Transcoder<D> coder, D value) {
            final Result<List<D>> listResult = coder.getList(value);
            if (!(listResult instanceof Result.Ok(List<D> list)))
                return listResult.cast();
            if (list.size() != 3)
                return new Result.Error<>("Invalid length for Vector, expected 3 but got " + list.size());
            final Result<Double> xResult = coder.getDouble(list.get(0));
            if (!(xResult instanceof Result.Ok(Double x)))
                return xResult.cast();
            final Result<Double> yResult = coder.getDouble(list.get(1));
            if (!(yResult instanceof Result.Ok(Double y)))
                return yResult.cast();
            final Result<Double> zResult = coder.getDouble(list.get(2));
            if (!(zResult instanceof Result.Ok(Double z)))
                return zResult.cast();
            return new Result.Ok<>(new Vec(x, y, z));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Point value) {
            if (value == null) return new Result.Error<>("null");
            final ListBuilder<D> list = coder.createList(3);
            list.add(coder.createDouble(value.x()));
            list.add(coder.createDouble(value.y()));
            list.add(coder.createDouble(value.z()));
            return new Result.Ok<>(list.build());
        }
    }

    record CompoundBinaryTagImpl() implements StructCodec<CompoundBinaryTag> {
        @Override
        public <D> Result<CompoundBinaryTag> decodeFromMap(Transcoder<D> coder, Transcoder.MapLike<D> map) {
            final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            for (String key : map.keys()) {
                final Result<BinaryTag> tagResult = map.getValue(key)
                        .map(nbt -> RawValue.of(coder, nbt).convertTo(Transcoder.NBT));
                if (!(tagResult instanceof Result.Ok(BinaryTag tag)))
                    return tagResult.mapError(e -> key + ": " + e).cast();
                builder.put(key, tag);
            }
            return new Result.Ok<>(builder.build());
        }

        @Override
        public <D> Result<D> encodeToMap(Transcoder<D> coder, CompoundBinaryTag value, Transcoder.MapBuilder<D> map) {
            for (var entry : value) {
                final Result<D> entryValue = RawValue.of(Transcoder.NBT, entry.getValue()).convertTo(coder);
                if (!(entryValue instanceof Result.Ok(D okValue)))
                    return entryValue.mapError(e -> entry.getKey() + ": " + e);
                map.put(entry.getKey(), okValue);
            }
            return new Result.Ok<>(map.build());
        }
    }

}
