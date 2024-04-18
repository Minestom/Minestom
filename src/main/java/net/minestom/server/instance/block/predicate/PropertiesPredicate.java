package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record PropertiesPredicate(@NotNull Map<String, ValuePredicate> properties) {

    public static final NetworkBuffer.Type<PropertiesPredicate> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, PropertiesPredicate value) {
            buffer.write(NetworkBuffer.VAR_INT, value.properties.size());
            for (Map.Entry<String, ValuePredicate> entry : value.properties.entrySet()) {
                buffer.write(NetworkBuffer.STRING, entry.getKey());
                buffer.write(ValuePredicate.NETWORK_TYPE, entry.getValue());
            }
        }

        @Override
        public PropertiesPredicate read(@NotNull NetworkBuffer buffer) {
            int size = buffer.read(NetworkBuffer.VAR_INT);
            Map<String, ValuePredicate> properties = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                properties.put(buffer.read(NetworkBuffer.STRING), buffer.read(ValuePredicate.NETWORK_TYPE));
            }
            return new PropertiesPredicate(properties);
        }
    };
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

    public PropertiesPredicate {
        properties = Map.copyOf(properties);
    }

    public sealed interface ValuePredicate permits ValuePredicate.Exact, ValuePredicate.Range {

        record Exact(@Nullable String value) implements ValuePredicate {

            public static final NetworkBuffer.Type<Exact> NETWORK_TYPE = NetworkBuffer.STRING.map(Exact::new, Exact::value);
            public static final BinaryTagSerializer<Exact> NBT_TYPE = BinaryTagSerializer.STRING.map(Exact::new, Exact::value);

        }

        record Range(@Nullable String min, @Nullable String max) implements ValuePredicate {

            public static final NetworkBuffer.Type<Range> NETWORK_TYPE = new NetworkBuffer.Type<>() {
                @Override
                public void write(@NotNull NetworkBuffer buffer, Range value) {
                    buffer.writeOptional(NetworkBuffer.STRING, value.min);
                    buffer.writeOptional(NetworkBuffer.STRING, value.max);
                }

                @Override
                public Range read(@NotNull NetworkBuffer buffer) {
                    return new Range(buffer.readOptional(NetworkBuffer.STRING), buffer.readOptional(NetworkBuffer.STRING));
                }
            };
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
