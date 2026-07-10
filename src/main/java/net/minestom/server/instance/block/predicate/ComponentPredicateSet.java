package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A list of {@link DataComponentPredicate}s.
 * <p>
 * Note: instances of this class are immutable. Calling {@link #add} or {@link #remove}
 * will return a new instance of this class with the element added or removed.
 */
public sealed interface ComponentPredicateSet extends Predicate<DataComponent.Holder> permits ComponentPredicateSetImpl {

    ComponentPredicateSet EMPTY = ComponentPredicateSetImpl.EMPTY;

    Codec<ComponentPredicateSet> CODEC = ComponentPredicateSetImpl.CODEC;
    NetworkBuffer.Type<ComponentPredicateSet> NETWORK_TYPE = ComponentPredicateSetImpl.NETWORK_TYPE;

    static ComponentPredicateSet of(Collection<DataComponentPredicate> predicates) {
        return new ComponentPredicateSetImpl(predicates);
    }

    @Contract(pure = true)
    Map<Integer, DataComponentPredicate> predicates();

    @Contract(pure = true)
    ComponentPredicateSet add(DataComponentPredicate predicate);

    @Contract(pure = true)
    ComponentPredicateSet remove(DataComponentPredicate predicate);

    boolean isEmpty();

    @Override
    default boolean test(DataComponent.Holder holder) {
        for (DataComponentPredicate predicate : predicates().values()) {
            if (!predicate.test(holder)) {
                return false;
            }
        }
        return true;
    }
}
