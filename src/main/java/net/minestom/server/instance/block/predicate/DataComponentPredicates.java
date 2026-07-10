package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.Range;

import java.util.*;
import java.util.function.Predicate;

public record DataComponentPredicates(DataComponentMap exact,
                                      ComponentPredicateSet predicates) implements Predicate<DataComponent.Holder> {

    public DataComponentPredicates {
        Objects.requireNonNull(exact, "Exact cannot be null. Use DataComponentMap.EMPTY to skip exact data component checks.");
        Objects.requireNonNull(predicates, "Component predicates cannot be null. Use ComponentPredicateSet.EMPTY to skip exact data component checks.");
    }

    public static final DataComponentPredicates EMPTY = new DataComponentPredicates(DataComponentMap.EMPTY, ComponentPredicateSet.EMPTY);

    public static final Codec<DataComponentPredicates> CODEC = StructCodec.struct(
            "components", DataComponent.PATCH_CODEC.optional(DataComponentMap.EMPTY), DataComponentPredicates::exact,
            "predicates", ComponentPredicateSet.CODEC.optional(ComponentPredicateSet.EMPTY), DataComponentPredicates::predicates,
            DataComponentPredicates::new
    );

    public static final NetworkBuffer.Type<DataComponentPredicates> NETWORK_TYPE = NetworkBufferTemplate.template(
            DataComponent.MAP_NETWORK_TYPE, DataComponentPredicates::exact,
            ComponentPredicateSet.NETWORK_TYPE, DataComponentPredicates::predicates,
            DataComponentPredicates::new
    );

    @Override
    public boolean test(DataComponent.Holder holder) {
        for (DataComponent.Value entry : exact.entrySet()) {
            if (!Objects.equals(holder.get(entry.component()), entry.value())) {
                return false;
            }
        }
        return predicates.test(holder);
    }
}
