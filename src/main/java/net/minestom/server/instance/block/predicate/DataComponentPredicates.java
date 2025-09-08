package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.Range;
import net.minestom.server.utils.validate.Check;

import java.util.*;
import java.util.function.Predicate;

public record DataComponentPredicates(DataComponentMap exact,
                                      ComponentPredicateSet predicates) implements Predicate<DataComponent.Holder> {

    public DataComponentPredicates {
        Check.notNull(exact, "Exact cannot be null. Use DataComponentMap.EMPTY to skip exact data component checks.");
        Check.notNull(predicates, "Component predicates cannot be null. Use ComponentPredicateSet.EMPTY to skip exact data component checks.");
    }

    public static final DataComponentPredicates EMPTY = new DataComponentPredicates(DataComponentMap.EMPTY, ComponentPredicateSet.EMPTY);

    public static final Codec<Range.Int> INT_RANGE_CODEC = StructCodec.struct(
            "min", Codec.INT.optional(), Range.Int::min,
            "max", Codec.INT.optional(), Range.Int::max,
            Range.Int::new
    ).orElse(Codec.INT.optional().transform(Range.Int::new, Range.Int::min));

    public static final Codec<Range.Double> DOUBLE_RANGE_CODEC = StructCodec.struct(
            "min", Codec.DOUBLE.optional(), Range.Double::min,
            "max", Codec.DOUBLE.optional(), Range.Double::max,
            Range.Double::new
    ).orElse(Codec.DOUBLE.optional().transform(Range.Double::new, Range.Double::min));

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
