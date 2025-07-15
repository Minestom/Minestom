package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface LevelBasedValue {

    StructCodec<LevelBasedValue> TAGGED_CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentLevelBasedValues, LevelBasedValue::codec, "type");
    Codec<LevelBasedValue> CODEC = new Codec<>() {
        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable LevelBasedValue value) {
            if (value instanceof Constant(float constantValue))
                return new Result.Ok<>(coder.createFloat(constantValue));
            return TAGGED_CODEC.encode(coder, value);
        }

        @Override
        public <D> Result<LevelBasedValue> decode(Transcoder<D> coder, D value) {
            final Result<Float> numberResult = coder.getFloat(value);
            if (numberResult instanceof Result.Ok(Float number))
                return new Result.Ok<>(new Constant(number));
            return TAGGED_CODEC.decode(coder, value);
        }
    };

    @ApiStatus.Internal
    static DynamicRegistry<StructCodec<? extends LevelBasedValue>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LevelBasedValue>> registry = DynamicRegistry.create(Key.key("minestom:enchantment_value_effect"));
        // Note that constant is omitted from the registry, it has serialization handled out of band above.
        registry.register("linear", Linear.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("clamped", Clamped.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("fraction", Fraction.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("levels_squared", LevelsSquared.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("lookup", Lookup.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float calc(int level);

    StructCodec<? extends LevelBasedValue> codec();

    record Constant(float value) implements LevelBasedValue {

        @Override
        public float calc(int level) {
            return value;
        }

        @Override
        public StructCodec<Constant> codec() {
            throw new UnsupportedOperationException("Constant values are serialized as a special case, see LevelBasedValue.CODEC");
        }
    }

    record Linear(float base, float perLevelAboveFirst) implements LevelBasedValue {
        public static final StructCodec<Linear> CODEC = StructCodec.struct(
                "base", Codec.FLOAT, Linear::base,
                "per_level_above_first", Codec.FLOAT, Linear::perLevelAboveFirst,
                Linear::new
        );

        @Override
        public float calc(int level) {
            return base + (perLevelAboveFirst * (level - 1));
        }

        @Override
        public StructCodec<Linear> codec() {
            return CODEC;
        }
    }

    record Clamped(LevelBasedValue value, float min, float max) implements LevelBasedValue {
        public static final StructCodec<Clamped> CODEC = StructCodec.struct(
                "value", LevelBasedValue.CODEC, Clamped::value,
                "min", Codec.FLOAT, Clamped::min,
                "max", Codec.FLOAT, Clamped::max,
                Clamped::new
        );

        @Override
        public float calc(int level) {
            return MathUtils.clamp(value.calc(level), min, max);
        }

        @Override
        public StructCodec<Clamped> codec() {
            return CODEC;
        }
    }

    record Fraction(LevelBasedValue numerator,
                    LevelBasedValue denominator) implements LevelBasedValue {
        public static final StructCodec<Fraction> CODEC = StructCodec.struct(
                "numerator", LevelBasedValue.CODEC, Fraction::numerator,
                "denominator", LevelBasedValue.CODEC, Fraction::denominator,
                Fraction::new
        );

        @Override
        public float calc(int level) {
            float denominator = this.denominator.calc(level);
            return denominator == 0f ? 0f : numerator.calc(level) / denominator;
        }

        @Override
        public StructCodec<Fraction> codec() {
            return CODEC;
        }
    }

    record LevelsSquared(float added) implements LevelBasedValue {
        public static final StructCodec<LevelsSquared> CODEC = StructCodec.struct(
                "added", Codec.FLOAT, LevelsSquared::added,
                LevelsSquared::new
        );

        @Override
        public float calc(int level) {
            return (level * level) + added;
        }

        @Override
        public StructCodec<LevelsSquared> codec() {
            return CODEC;
        }
    }

    record Lookup(List<Float> values, LevelBasedValue fallback) implements LevelBasedValue {
        public static final StructCodec<Lookup> CODEC = StructCodec.struct(
                "values", Codec.FLOAT.list(), Lookup::values,
                "fallback", LevelBasedValue.CODEC, Lookup::fallback,
                Lookup::new
        );

        @Override
        public float calc(int level) {
            if (level < 0 || level > values.size()) return fallback.calc(level);
            return values.get(level - 1);
        }

        @Override
        public StructCodec<Lookup> codec() {
            return CODEC;
        }
    }

}
