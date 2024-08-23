package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record PropertiesPredicate(@NotNull Map<String, ValuePredicate> properties) implements Predicate<Block> {

    public static final NetworkBuffer.Type<PropertiesPredicate> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING.mapValue(ValuePredicate.NETWORK_TYPE), PropertiesPredicate::properties,
            PropertiesPredicate::new
    );
    public static final BinaryTagSerializer<PropertiesPredicate> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                Map<String, ValuePredicate> properties = new HashMap<>();
                for (Map.Entry<String, ? extends BinaryTag> entry : tag) {
                    properties.put(entry.getKey(), ValuePredicate.NBT_TYPE.read(entry.getValue()));
                }
                return new PropertiesPredicate(properties);
            },
            value -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Map.Entry<String, ValuePredicate> entry : value.properties.entrySet()) {
                    builder.put(entry.getKey(), ValuePredicate.NBT_TYPE.write(entry.getValue()));
                }
                return builder.build();
            }
    );

    public static @NotNull PropertiesPredicate exact(@NotNull String key, @NotNull String value) {
        return new PropertiesPredicate(Map.of(key, new ValuePredicate.Exact(value)));
    }

    public PropertiesPredicate {
        properties = Map.copyOf(properties);
    }

    @Override
    public boolean test(@NotNull Block block) {
        for (Map.Entry<String, ValuePredicate> entry : properties.entrySet()) {
            final String value = block.getProperty(entry.getKey());
            if (!entry.getValue().test(value))
                return false;
        }
        return true;
    }

    public sealed interface ValuePredicate extends Predicate<@Nullable String> permits ValuePredicate.Exact, ValuePredicate.Range {

        record Exact(@Nullable String value) implements ValuePredicate {

            public static final NetworkBuffer.Type<Exact> NETWORK_TYPE = NetworkBuffer.STRING.transform(Exact::new, Exact::value);
            public static final BinaryTagSerializer<Exact> NBT_TYPE = BinaryTagSerializer.STRING.map(Exact::new, Exact::value);

            @Override
            public boolean test(@Nullable String prop) {
                return prop != null && prop.equals(value);
            }
        }

        /**
         * <p>Vanilla has some fancy behavior to get integer properties as ints, but seems to just compare the value
         * anyway if its a string. Our behavior here is to attempt to parse the values as an integer and default
         * to a string.compareTo otherwise.</p>
         *
         * <p>Providing no min or max or a property which does exist results in a constant false.</p>
         *
         * @param min The min value to match, inclusive
         * @param max The max value to match, exclusive
         */
        record Range(@Nullable String min, @Nullable String max) implements ValuePredicate {
            public static final NetworkBuffer.Type<Range> NETWORK_TYPE = NetworkBufferTemplate.template(
                    STRING.optional(), Range::min,
                    STRING.optional(), Range::max,
                    Range::new
            );

            public static final BinaryTagSerializer<Range> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                    tag -> new Range(
                            tag.get("min") instanceof StringBinaryTag string ? string.value() : null,
                            tag.get("max") instanceof StringBinaryTag string ? string.value() : null),
                    value -> {
                        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                        if (value.min != null) builder.putString("min", value.min);
                        if (value.max != null) builder.putString("max", value.max);
                        return builder.build();
                    }
            );

            @Override
            public boolean test(@Nullable String prop) {
                if (prop == null || (min == null && max == null)) return false;
                try {
                    // Try to match as integers
                    int value = Integer.parseInt(prop);
                    return (min == null || value >= Integer.parseInt(min))
                            && (max == null || value < Integer.parseInt(max));
                } catch (NumberFormatException e) {
                    // Not an integer, just compare the strings
                    return (min == null || prop.compareTo(min) >= 0)
                            && (max == null || prop.compareTo(max) < 0);
                }
            }
        }

        NetworkBuffer.Type<ValuePredicate> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, ValuePredicate value) {
                switch (value) {
                    case Exact exact -> {
                        buffer.write(NetworkBuffer.BOOLEAN, true);
                        buffer.write(Exact.NETWORK_TYPE, exact);
                    }
                    case Range range -> {
                        buffer.write(NetworkBuffer.BOOLEAN, false);
                        buffer.write(Range.NETWORK_TYPE, range);
                    }
                }
            }

            @Override
            public ValuePredicate read(@NotNull NetworkBuffer buffer) {
                return buffer.read(NetworkBuffer.BOOLEAN) ? buffer.read(Exact.NETWORK_TYPE) : buffer.read(Range.NETWORK_TYPE);
            }
        };
        BinaryTagSerializer<ValuePredicate> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull ValuePredicate value) {
                return switch (value) {
                    case Exact exact -> Exact.NBT_TYPE.write(exact);
                    case Range range -> Range.NBT_TYPE.write(range);
                };
            }

            @Override
            public @NotNull ValuePredicate read(@NotNull BinaryTag tag) {
                if (tag instanceof StringBinaryTag) {
                    return Exact.NBT_TYPE.read(tag);
                } else {
                    return Range.NBT_TYPE.read(tag);
                }
            }
        };
    }
}
