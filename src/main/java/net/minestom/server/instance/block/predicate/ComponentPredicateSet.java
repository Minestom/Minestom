package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A list of {@link DataComponentPredicate}s.
 * <p>
 * Note: instances of this class are immutable. Calling {@link #add} or {@link #remove}
 * will return a new instance of this class with the element added or removed.
 */
public record ComponentPredicateSet(List<DataComponentPredicate> predicates) implements Predicate<DataComponent.Holder> {

    private static final int MAX_NETWORK_SIZE = 64;
    private static final Codec<Either<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponent<?>>> PREDICATE_TYPE_CODEC =
            Codec.Either(RegistryKey.codec(Registries::componentPredicateTypes), DataComponent.CODEC);
    private static final NetworkBuffer.Type<Either<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponent<?>>> PREDICATE_TYPE_NETWORK_TYPE =
            NetworkBuffer.Either(RegistryKey.networkType(Registries::componentPredicateTypes), DataComponent.NETWORK_TYPE);
    private static final NetworkBuffer.Type<DataComponentPredicate> PREDICATE_NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, DataComponentPredicate predicate) {
            switch (predicate) {
                case DataComponentPredicate.Exists(var component) -> {
                    buffer.write(PREDICATE_TYPE_NETWORK_TYPE, Either.right(component));
                    buffer.write(NetworkBuffer.TypedNBT(Codec.UNIT), Unit.INSTANCE);
                }
                case DataComponentPredicate.Registered registered -> {
                    final var predicateCodec = registered.codec();
                    final var registries = Objects.requireNonNull(buffer.registries(), "Missing registries in buffer");
                    final var registry = registries.componentPredicateTypes();
                    final var key = Objects.requireNonNull(registry.getKey(predicateCodec),
                            "Unknown DataComponentPredicate type");
                    buffer.write(PREDICATE_TYPE_NETWORK_TYPE, Either.left(key));
                    @SuppressWarnings("unchecked")
                    final Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) (Codec<?>) predicateCodec;
                    buffer.write(NetworkBuffer.TypedNBT(codec), predicate);
                }
            }
        }

        @Override
        public DataComponentPredicate read(NetworkBuffer buffer) {
            return switch (buffer.read(PREDICATE_TYPE_NETWORK_TYPE)) {
                case Either.Left(var key) -> {
                    final var registries = Objects.requireNonNull(buffer.registries(), "Missing registries in buffer");
                    final var registry = registries.componentPredicateTypes();
                    @SuppressWarnings("unchecked")
                    final Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) Objects.requireNonNull(
                            registry.get(key), "Unknown DataComponentPredicate type");
                    yield buffer.read(NetworkBuffer.TypedNBT(codec));
                }
                case Either.Right(var component) -> {
                    buffer.read(NetworkBuffer.TypedNBT(Codec.UNIT));
                    yield new DataComponentPredicate.Exists(component);
                }
            };
        }
    };
    public static final ComponentPredicateSet EMPTY = new ComponentPredicateSet(List.of());

    // Need dispatch on each key, TODO another registry will likely need this eventually.
    public static final Codec<ComponentPredicateSet> CODEC = new Codec<>() {
        @Override
        public <D> Result<ComponentPredicateSet> decode(Transcoder<D> coder, D value) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = context.registries().componentPredicateTypes();
            final Result<Transcoder.MapLike<D>> mapResult = coder.getMap(value);
            if (!(mapResult instanceof Result.Ok(Transcoder.MapLike<D> map)))
                return mapResult.cast();

            final List<DataComponentPredicate> predicates = new ArrayList<>(map.size());
            for (final String name : map.keys()) {
                final Result<Either<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponent<?>>> typeResult =
                        PREDICATE_TYPE_CODEC.decode(coder, coder.createString(name));
                if (!(typeResult instanceof Result.Ok(var type)))
                    return typeResult.mapError(error -> name + ": " + error).cast();
                final Result<D> valueResult = map.getValue(name);
                if (!(valueResult instanceof Result.Ok(D encodedPredicate)))
                    return valueResult.mapError(error -> name + ": " + error).cast();
                switch (type) {
                    case Either.Left(var key) -> {
                        final Codec<? extends DataComponentPredicate> codec = registry.get(key);
                        if (codec == null) return new Result.Error<>("Unknown component predicate type: " + name);
                        final Result<? extends DataComponentPredicate> predicateResult = codec.decode(coder, encodedPredicate);
                        if (!(predicateResult instanceof Result.Ok<? extends DataComponentPredicate>(var predicate)))
                            return predicateResult.mapError(error -> name + ": " + error).cast();
                        predicates.add(predicate);
                    }
                    case Either.Right(var component) -> {
                        final Result<Unit> unitResult = Codec.UNIT.decode(coder, encodedPredicate);
                        if (!(unitResult instanceof Result.Ok<Unit>))
                            return unitResult.mapError(error -> name + ": " + error).cast();
                        predicates.add(new DataComponentPredicate.Exists(component));
                    }
                }
            }
            return new Result.Ok<>(new ComponentPredicateSet(predicates));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable ComponentPredicateSet value) {
            if (value == null) return new Result.Error<>("null");
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = context.registries().componentPredicateTypes();
            final Transcoder.MapBuilder<D> map = coder.createMap();
            for (final DataComponentPredicate predicate : value.predicates()) {
                final Either<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponent<?>> type;
                final Result<D> predicateResult;
                switch (predicate) {
                    case DataComponentPredicate.Exists(var component) -> {
                        type = Either.right(component);
                        predicateResult = Codec.UNIT.encode(coder, Unit.INSTANCE);
                    }
                    case DataComponentPredicate.Registered registered -> {
                        final var predicateCodec = registered.codec();
                        final RegistryKey<Codec<? extends DataComponentPredicate>> key = registry.getKey(predicateCodec);
                        if (key == null) return new Result.Error<>("Unregistered component predicate type: " + predicate);
                        type = Either.left(key);
                        @SuppressWarnings("unchecked")
                        final Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) (Codec<?>) predicateCodec;
                        predicateResult = codec.encode(coder, predicate);
                    }
                }
                final Result<D> keyResult = PREDICATE_TYPE_CODEC.encode(coder, type);
                if (!(keyResult instanceof Result.Ok(D encodedKey))) return keyResult.cast();
                if (!(predicateResult instanceof Result.Ok(D encodedPredicate)))
                    return predicateResult.mapError(error -> predicate + ": " + error).cast();
                map.put(encodedKey, encodedPredicate);
            }
            return new Result.Ok<>(map.build());
        }
    };

    public static final NetworkBuffer.Type<ComponentPredicateSet> NETWORK_TYPE = PREDICATE_NETWORK_TYPE
            .list(MAX_NETWORK_SIZE)
            .transform(ComponentPredicateSet::new, ComponentPredicateSet::networkPredicates);

    public ComponentPredicateSet {
        predicates = List.copyOf(predicates);
    }

    @Contract(pure = true)
    public ComponentPredicateSet add(DataComponentPredicate predicate) {
        Objects.requireNonNull(predicate, "predicate");
        var predicates = new ArrayList<DataComponentPredicate>(this.predicates.size() + 1);
        for (DataComponentPredicate existing : this.predicates) {
            if (!sameType(existing, predicate)) predicates.add(existing);
        }
        predicates.add(predicate);
        return new ComponentPredicateSet(predicates);
    }

    @Contract(pure = true)
    public ComponentPredicateSet remove(DataComponentPredicate predicate) {
        Objects.requireNonNull(predicate, "predicate");
        var predicates = new ArrayList<DataComponentPredicate>(this.predicates.size());
        for (DataComponentPredicate existing : this.predicates) {
            if (!sameType(existing, predicate)) predicates.add(existing);
        }
        return new ComponentPredicateSet(predicates);
    }

    public boolean isEmpty() {
        return predicates.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ComponentPredicateSet(var otherPredicates) &&
                predicates.size() == otherPredicates.size() && predicates.containsAll(otherPredicates));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (final DataComponentPredicate predicate : predicates) hash += predicate.hashCode();
        return hash;
    }

    private static boolean sameType(DataComponentPredicate first, DataComponentPredicate second) {
        return switch (first) {
            case DataComponentPredicate.Exists(var firstComponent) ->
                    second instanceof DataComponentPredicate.Exists(var secondComponent) &&
                            firstComponent == secondComponent;
            case DataComponentPredicate.Registered firstRegistered ->
                    second instanceof DataComponentPredicate.Registered secondRegistered &&
                            firstRegistered.codec() == secondRegistered.codec();
        };
    }

    private static List<DataComponentPredicate> networkPredicates(ComponentPredicateSet value) {
        Check.argCondition(value.predicates.size() > MAX_NETWORK_SIZE,
                "Component predicate count ({0}) is higher than the maximum allowed size ({1})",
                value.predicates.size(), MAX_NETWORK_SIZE);
        return value.predicates;
    }

    @Override
    public boolean test(DataComponent.Holder holder) {
        for (DataComponentPredicate predicate : predicates()) {
            if (!predicate.test(holder)) {
                return false;
            }
        }
        return true;
    }
}
