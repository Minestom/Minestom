package net.minestom.server.instance.block.predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

record ComponentPredicateSetImpl(Map<Integer, DataComponentPredicate> predicates) implements ComponentPredicateSet {

    static final ComponentPredicateSetImpl EMPTY = new ComponentPredicateSetImpl(Int2ObjectMaps.emptyMap());

    ComponentPredicateSetImpl {
        predicates = Map.copyOf(predicates);
    }

    ComponentPredicateSetImpl(Collection<DataComponentPredicate> predicates) {
        this(createMap(predicates));
    }

    private static Map<Integer, DataComponentPredicate> createMap(Collection<DataComponentPredicate> predicates) {
        Map<Integer, DataComponentPredicate> map = new Int2ObjectArrayMap<>(predicates.size());
        for (DataComponentPredicate predicate : predicates) {
            var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
            Objects.requireNonNull(key, "Unknown DataComponentPredicate type");
            map.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        }
        return map;
    }

    static final Codec<ComponentPredicateSet> CODEC = RegistryKey.codec(Registries::componentPredicateTypes)
            .mapValueTyped((key) -> {
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) MinecraftServer.componentPredicateTypes().get(key);
                Objects.requireNonNull(codec, "Unknown DataComponentPredicate type");
                return codec;
            })
            .transform(ComponentPredicateSetImpl::fromMap, ComponentPredicateSetImpl::toMap);

    static final NetworkBuffer.Type<ComponentPredicateSet> NETWORK_TYPE = new NetworkBuffer.Type<ComponentPredicateSet>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ComponentPredicateSet value) {
            buffer.write(NetworkBuffer.VAR_INT, value.predicates().size());
            for (var entry : value.predicates().entrySet()) {
                var key = MinecraftServer.componentPredicateTypes().getKey(entry.getValue().codec());
                Objects.requireNonNull(key, "Unknown DataComponentPredicate type");
                buffer.write(RegistryKey.networkType(Registries::componentPredicateTypes), key);
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) MinecraftServer.componentPredicateTypes().get(key);
                buffer.write(NetworkBuffer.TypedNBT(codec), entry.getValue());
            }
        }

        @Override
        public ComponentPredicateSet read(@NotNull NetworkBuffer buffer) {
            int size = buffer.read(NetworkBuffer.VAR_INT);
            Map<Integer, DataComponentPredicate> map = new Int2ObjectArrayMap<>(size);
            for (int i = 0; i < size; i++) {
                var key = buffer.read(RegistryKey.networkType(Registries::componentPredicateTypes));
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) MinecraftServer.componentPredicateTypes().get(key);
                Objects.requireNonNull(codec, "Unknown DataComponentPredicate type");
                var predicate = buffer.read(NetworkBuffer.TypedNBT(codec));
                map.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
            }
            return new ComponentPredicateSetImpl(map);
        }
    };

    private static ComponentPredicateSet fromMap(Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> input) {
        Map<Integer, DataComponentPredicate> map = new Int2ObjectArrayMap<>(input.size());
        for (DataComponentPredicate predicate : input.values()) {
            var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
            Objects.requireNonNull(key, "Unknown DataComponentPredicate type");
            map.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        }
        return new ComponentPredicateSetImpl(map);
    }

    private static Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> toMap(ComponentPredicateSet value) {
        Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> map = new HashMap<>(value.predicates().size());
        for (Map.Entry<Integer, DataComponentPredicate> entry : value.predicates().entrySet()) {
            var key = MinecraftServer.componentPredicateTypes().getKey(entry.getKey());
            assert key != null;
            map.put(key, entry.getValue());
        }
        return map;
    }

    @Override
    public @NotNull ComponentPredicateSet add(@NotNull DataComponentPredicate predicate) {
        var newMap = new Int2ObjectArrayMap<DataComponentPredicate>(predicates.size() + 1);
        newMap.putAll(predicates);
        var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
        Objects.requireNonNull(key, "Unknown DataComponentPredicate type");
        newMap.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        return new ComponentPredicateSetImpl(newMap);
    }

    @Override
    public @NotNull ComponentPredicateSet remove(@NotNull DataComponentPredicate predicate) {
        var newMap = new Int2ObjectArrayMap<>(predicates);
        var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
        Objects.requireNonNull(key, "Unknown DataComponentPredicate type");
        newMap.remove(MinecraftServer.componentPredicateTypes().getId(key));
        return new ComponentPredicateSetImpl(newMap);
    }

    @Override
    public boolean isEmpty() {
        return predicates.isEmpty();
    }

    @Override
    public String toString() {
        return "ComponentPredicateSet{" +
                "predicates=" + predicates.values() +
                '}';
    }
}
