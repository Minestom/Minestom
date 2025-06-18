package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LevelBasedValue {

    @NotNull StructCodec<LevelBasedValue> TAGGED_CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentLevelBasedValues, LevelBasedValue::codec, "type");
    @NotNull Codec<LevelBasedValue> CODEC = new Codec<>() {
        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable LevelBasedValue value) {
            if (value instanceof Constant(float constantValue))
                return new Result.Ok<>(coder.createFloat(constantValue));
            return TAGGED_CODEC.encode(coder, value);
        }

        @Override
        public @NotNull <D> Result<LevelBasedValue> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Float> numberResult = coder.getFloat(value);
            if (numberResult instanceof Result.Ok(Float number))
                return new Result.Ok<>(new Constant(number));
            return TAGGED_CODEC.decode(coder, value);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends LevelBasedValue>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LevelBasedValue>> registry = DynamicRegistry.create(RegistryKey.unsafeOf("minestom:enchantment_level_based_value"));
        // Note that constant is omitted from the registry, it has serialization handled out of band above.
        registry.register("linear", Linear.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("clamped", Clamped.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("fraction", Fraction.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("levels_squared", LevelsSquared.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("lookup", Lookup.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float calc(int level);

    @NotNull StructCodec<? extends LevelBasedValue> codec();

    record Constant(float value) implements LevelBasedValue {

        @Override
        public float calc(int level) {
            return value;
        }

        @Override
        public @NotNull StructCodec<Constant> codec() {
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
        public @NotNull StructCodec<Linear> codec() {
            return CODEC;
        }
    }

    record Clamped(@NotNull LevelBasedValue value, float min, float max) implements LevelBasedValue {
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
        public @NotNull StructCodec<Clamped> codec() {
            return CODEC;
        }
    }

    record Fraction(@NotNull LevelBasedValue numerator,
                    @NotNull LevelBasedValue denominator) implements LevelBasedValue {
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
        public @NotNull StructCodec<Fraction> codec() {
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
        public @NotNull StructCodec<LevelsSquared> codec() {
            return CODEC;
        }
    }

    record Lookup(@NotNull List<Float> values, @NotNull LevelBasedValue fallback) implements LevelBasedValue {
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
        public @NotNull StructCodec<Lookup> codec() {
            return CODEC;
        }
    }

}
