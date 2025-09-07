package net.minestom.server.instance.block.predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;

public record ComponentPredicateSet(Map<Integer, DataComponentPredicate> predicates) {

    public static final ComponentPredicateSet EMPTY = new ComponentPredicateSet();

    public ComponentPredicateSet() {
        this(new Int2ObjectArrayMap<>());
    }

    public ComponentPredicateSet(Map<Integer, DataComponentPredicate> predicates) {
        this.predicates = new Int2ObjectArrayMap<>(predicates);
    }

    public static final Codec<ComponentPredicateSet> CODEC = RegistryKey.codec(Registries::componentPredicateTypes)
            .mapValue((key) -> {
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) MinecraftServer.componentPredicateTypes().get(key);
                Check.notNull(codec, "Unknown DataComponentPredicate type");
                return codec;
            })
            .transform(ComponentPredicateSet::fromMap, ComponentPredicateSet::toMap);

    public static final NetworkBuffer.Type<ComponentPredicateSet> NETWORK_TYPE = RegistryKey.networkType(Registries::componentPredicateTypes)
            .mapValue((key) -> {
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) MinecraftServer.componentPredicateTypes().get(key);
                Check.notNull(codec, "Unknown DataComponentPredicate type");
                return NetworkBuffer.TypedNBT(codec);
            })
            .transform(ComponentPredicateSet::fromMap, ComponentPredicateSet::toMap);

    @Contract(pure = true)
    private Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> toMap() {
        Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> map = new HashMap<>(this.predicates.size());
        for (Map.Entry<Integer, DataComponentPredicate> entry : predicates.entrySet()) {
            var key = MinecraftServer.componentPredicateTypes().getKey(entry.getKey());
            map.put(key, entry.getValue());
        }
        return map;
    }

    private static ComponentPredicateSet fromMap(Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> input) {
        Int2ObjectArrayMap<DataComponentPredicate> map = new Int2ObjectArrayMap<>(input.size());
        for (DataComponentPredicate predicate : input.values()) {
            var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
            Check.notNull(key, "Unknown DataComponentPredicate type");
            map.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        }
        return new ComponentPredicateSet(map);
    }

    @Contract(pure = true)
    public ComponentPredicateSet add(DataComponentPredicate predicate) {
        var newMap = new Int2ObjectArrayMap<>(predicates);
        var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
        Check.notNull(key, "Unknown DataComponentPredicate type");
        newMap.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        return new ComponentPredicateSet(newMap);
    }

    @Contract(pure = true)
    public ComponentPredicateSet remove(DataComponentPredicate predicate) {
        var newMap = new Int2ObjectArrayMap<>(predicates);
        var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
        Check.notNull(key, "Unknown DataComponentPredicate type");
        newMap.remove(MinecraftServer.componentPredicateTypes().getId(key));
        return new ComponentPredicateSet(newMap);
    }

    public boolean isEmpty() {
        return predicates.isEmpty();
    }

    public boolean test(DataComponent.Holder holder) {
        for (DataComponentPredicate predicate : predicates.values()) {
            if (!predicate.test(holder)) {
                return false;
            }
        }
        return true;
    }
}
