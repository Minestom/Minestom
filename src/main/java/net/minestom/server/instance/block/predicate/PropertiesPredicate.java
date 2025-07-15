package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record PropertiesPredicate(Map<String, ValuePredicate> properties) implements Predicate<Block> {

    public static final NetworkBuffer.Type<PropertiesPredicate> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING.mapValue(ValuePredicate.NETWORK_TYPE), PropertiesPredicate::properties,
            PropertiesPredicate::new
    );
    public static final Codec<PropertiesPredicate> CODEC = Codec.STRING.mapValue(ValuePredicate.CODEC)
            .transform(PropertiesPredicate::new, PropertiesPredicate::properties);

    public static PropertiesPredicate exact(String key, String value) {
        return new PropertiesPredicate(Map.of(key, new ValuePredicate.Exact(value)));
    }

    public PropertiesPredicate {
        properties = Map.copyOf(properties);
    }

    @Override
    public boolean test(Block block) {
        for (Map.Entry<String, ValuePredicate> entry : properties.entrySet()) {
            final String value = block.getProperty(entry.getKey());
            if (!entry.getValue().test(value))
                return false;
        }
        return true;
    }

    public sealed interface ValuePredicate extends Predicate<@Nullable String> permits ValuePredicate.Exact, ValuePredicate.Range {
        NetworkBuffer.Type<ValuePredicate> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, ValuePredicate value) {
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
            public ValuePredicate read(NetworkBuffer buffer) {
                return buffer.read(NetworkBuffer.BOOLEAN) ? buffer.read(Exact.NETWORK_TYPE) : buffer.read(Range.NETWORK_TYPE);
            }
        };
        Codec<ValuePredicate> CODEC = new Codec<>() {
            @Override
            public <D> Result<ValuePredicate> decode(Transcoder<D> coder, D value) {
                final Result<Exact> exactResult = Exact.CODEC.decode(coder, value);
                if (exactResult instanceof Result.Ok(Exact exact))
                    return new Result.Ok<>(exact);
                final Result<Range> rangeResult = Range.CODEC.decode(coder, value);
                if (rangeResult instanceof Result.Ok(Range range))
                    return new Result.Ok<>(range);
                return new Result.Error<>("Invalid value predicate");
            }

            @Override
            public <D> Result<D> encode(Transcoder<D> coder, @Nullable ValuePredicate value) {
                if (value == null) return new Result.Error<>("null");
                return switch (value) {
                    case Exact exact -> Exact.CODEC.encode(coder, exact);
                    case Range range -> Range.CODEC.encode(coder, range);
                };
            }
        };

        record Exact(@Nullable String value) implements ValuePredicate {

            public static final NetworkBuffer.Type<Exact> NETWORK_TYPE = NetworkBuffer.STRING.transform(Exact::new, Exact::value);
            public static final Codec<Exact> CODEC = Codec.STRING.transform(Exact::new, Exact::value);

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
                    Range::new);
            public static final Codec<Range> CODEC = StructCodec.struct(
                    "min", Codec.STRING.optional(), Range::min,
                    "max", Codec.STRING.optional(), Range::max,
                    Range::new);

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
    }
}
