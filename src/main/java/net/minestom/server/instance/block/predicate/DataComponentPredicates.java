package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public record DataComponentPredicates(@Nullable DataComponentMap exact,
                                      @Nullable ComponentPredicateSet predicates) implements Predicate<DataComponent.Holder> {

    public static final DataComponentPredicates EMPTY = new DataComponentPredicates(null, null);

    public static final Codec<Range.Int> INT_RANGE_CODEC = StructCodec.struct(
            "min", Codec.INT, Range.Int::min,
            "max", Codec.INT, Range.Int::max,
            Range.Int::new
    ).orElse(Codec.INT.transform(Range.Int::new, Range.Int::min));

    public static final Codec<Range.Double> DOUBLE_RANGE_CODEC = StructCodec.struct(
            "min", Codec.DOUBLE, Range.Double::min,
            "max", Codec.DOUBLE, Range.Double::max,
            Range.Double::new
    ).orElse(Codec.DOUBLE.transform(Range.Double::new, Range.Double::min));

    public static final Codec<DataComponentPredicates> CODEC = StructCodec.struct(
            "components", DataComponent.PATCH_CODEC.optional(), DataComponentPredicates::exact,
            "predicates", ComponentPredicateSet.CODEC.optional(), DataComponentPredicates::predicates,
            DataComponentPredicates::new
    );

    public static final NetworkBuffer.Type<DataComponentPredicates> NETWORK_TYPE = new NetworkBuffer.Type<>() {

        private static final NetworkBuffer.Type<DataComponentPredicates> delegate = NetworkBufferTemplate.template(
                DataComponent.MAP_NETWORK_TYPE.transform(Function.identity(), map -> Objects.requireNonNullElse(map, DataComponentMap.EMPTY)), DataComponentPredicates::exact,
                ComponentPredicateSet.NETWORK_TYPE.transform(Function.identity(), map -> Objects.requireNonNullElse(map, ComponentPredicateSet.EMPTY)), DataComponentPredicates::predicates,
                DataComponentPredicates::new
        );

        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponentPredicates value) {
            delegate.write(buffer, Objects.requireNonNullElseGet(value, () -> new DataComponentPredicates(null, null)));
        }

        @Override
        public @NotNull DataComponentPredicates read(@NotNull NetworkBuffer buffer) {
            DataComponentPredicates value = delegate.read(buffer);
            DataComponentMap exact = value.exact();
            // Read empty lists and compounds as null
            if (exact != null && exact.isEmpty()) {
                exact = null;
            }
            ComponentPredicateSet predicates = value.predicates();
            if (predicates != null && predicates.isEmpty()) {
                predicates = null;
            }
            return new DataComponentPredicates(exact, predicates);
        }
    };

    @Override
    public boolean test(@NotNull DataComponent.Holder holder) {
        if (exact != null && !exact.entrySet().stream().allMatch(entry -> Objects.equals(holder.get(entry.component()), entry.value()))) {
            return false;
        }
        return predicates == null || predicates.test(holder);
    }
}
