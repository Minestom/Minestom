package net.minestom.server.instance.block.predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;

import java.util.*;
import java.util.function.Predicate;

/**
 * A list of {@link DataComponentPredicate}s.
 * <p>
 * Note: instances of this class are immutable. Calling {@link #add} or {@link #remove}
 * will return a new instance of this class with the element added or removed.
 *
 */
public class ComponentPredicateSet implements Predicate<DataComponent.Holder> {

    public static final ComponentPredicateSet EMPTY = new ComponentPredicateSet();

    /**
     * A map of DataComponentPredicate registry entry IDs to their values
     */
    private final Map<Integer, DataComponentPredicate> predicates;

    public ComponentPredicateSet() {
        this(Int2ObjectMaps.emptyMap());
    }

    public ComponentPredicateSet(Collection<DataComponentPredicate> predicates) {
        this.predicates = new Int2ObjectArrayMap<>(predicates.size());
        for (DataComponentPredicate predicate : predicates) {
            var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
            Check.notNull(key, "Unknown DataComponentPredicate type");
            this.predicates.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        }
    }

    private ComponentPredicateSet(Map<Integer, DataComponentPredicate> predicates) {
        this.predicates = predicates;
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

    private static ComponentPredicateSet fromMap(Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> input) {
        Int2ObjectArrayMap<DataComponentPredicate> map = new Int2ObjectArrayMap<>(input.size());
        for (DataComponentPredicate predicate : input.values()) {
            var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
            Check.notNull(key, "Unknown DataComponentPredicate type");
            map.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        }
        return new ComponentPredicateSet(map);
    }

    /**
     * Converts this {@link ComponentPredicateSet} to a Map.
     * @return a map of {@link DataComponentPredicate} registry keys to their values
     */
    @Contract(pure = true)
    private Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> toMap() {
        Map<RegistryKey<Codec<? extends DataComponentPredicate>>, DataComponentPredicate> map = new HashMap<>(this.predicates.size());
        for (Map.Entry<Integer, DataComponentPredicate> entry : predicates.entrySet()) {
            var key = MinecraftServer.componentPredicateTypes().getKey(entry.getKey());
            assert key != null;
            map.put(key, entry.getValue());
        }
        return map;
    }

    /**
     * Returns a new {@link ComponentPredicateSet} with the predicate added.
     * @param predicate The predicate to add
     * @return a new {@link ComponentPredicateSet} with the predicate added
     */
    @Contract(pure = true)
    public ComponentPredicateSet add(DataComponentPredicate predicate) {
        var newMap = new Int2ObjectArrayMap<DataComponentPredicate>(predicates.size() + 1);
        newMap.putAll(predicates);
        var key = MinecraftServer.componentPredicateTypes().getKey(predicate.codec());
        Check.notNull(key, "Unknown DataComponentPredicate type");
        newMap.put(MinecraftServer.componentPredicateTypes().getId(key), predicate);
        return new ComponentPredicateSet(newMap);
    }

    /**
     * Returns a new {@link ComponentPredicateSet} with the predicate removed.
     * @param predicate The predicate to remove
     * @return a new {@link ComponentPredicateSet} with the predicate removed
     */
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

    @Override
    public boolean test(DataComponent.Holder holder) {
        for (DataComponentPredicate predicate : predicates.values()) {
            if (!predicate.test(holder)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComponentPredicateSet that)) return false;
        return Objects.equals(predicates, that.predicates);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(predicates);
    }

    @Override
    public String toString() {
        return "ComponentPredicateSet{" +
                "predicates=" + predicates.values() +
                '}';
    }
}
