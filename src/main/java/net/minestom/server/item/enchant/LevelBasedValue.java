package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.nbt.FloatBinaryTag.floatBinaryTag;

public interface LevelBasedValue {

    @NotNull BinaryTagSerializer<LevelBasedValue> TAGGED_CODEC = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentLevelBasedValues, LevelBasedValue::codec, "type");
    @NotNull Codec<LevelBasedValue> CODEC = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull LevelBasedValue value) {
            if (value instanceof Constant constant) return floatBinaryTag(constant.value);
            return TAGGED_CODEC.write(context, value);
        }

        @Override
        public @NotNull LevelBasedValue read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (tag instanceof NumberBinaryTag number) return new Constant(number.floatValue());
            return TAGGED_CODEC.read(context, tag);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Codec<? extends LevelBasedValue>> createDefaultRegistry() {
        final DynamicRegistry<Codec<? extends LevelBasedValue>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        // Note that constant is omitted from the registry, it has serialization handled out of band above.
        registry.register("linear", Linear.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("clamped", Clamped.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("fraction", Fraction.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("levels_squared", LevelsSquared.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("lookup", Lookup.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float calc(int level);

    @NotNull Codec<? extends LevelBasedValue> codec();

    record Constant(float value) implements LevelBasedValue {

        @Override
        public float calc(int level) {
            return value;
        }

        @Override
        public @NotNull Codec<Constant> codec() {
            throw new UnsupportedOperationException("Constant values are serialized as a special case, see LevelBasedValue.CODEC");
        }
    }

    record Linear(float base, float perLevelAboveFirst) implements LevelBasedValue {
        public static final Codec<Linear> CODEC = StructCodec.struct(
                "base", Codec.FLOAT, Linear::base,
                "per_level_above_first", Codec.FLOAT, Linear::perLevelAboveFirst,
                Linear::new
        );

        @Override
        public float calc(int level) {
            return base + (perLevelAboveFirst * (level - 1));
        }

        @Override
        public @NotNull Codec<Linear> codec() {
            return CODEC;
        }
    }

    record Clamped(@NotNull LevelBasedValue value, float min, float max) implements LevelBasedValue {
        public static final Codec<Clamped> CODEC = StructCodec.struct(
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
        public @NotNull Codec<Clamped> codec() {
            return CODEC;
        }
    }

    record Fraction(@NotNull LevelBasedValue numerator,
                    @NotNull LevelBasedValue denominator) implements LevelBasedValue {
        public static final Codec<Fraction> CODEC = StructCodec.struct(
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
        public @NotNull Codec<Fraction> codec() {
            return CODEC;
        }
    }

    record LevelsSquared(float added) implements LevelBasedValue {
        public static final Codec<LevelsSquared> CODEC = StructCodec.struct(
                "added", Codec.FLOAT, LevelsSquared::added,
                LevelsSquared::new
        );

        @Override
        public float calc(int level) {
            return (level * level) + added;
        }

        @Override
        public @NotNull Codec<LevelsSquared> codec() {
            return CODEC;
        }
    }

    record Lookup(@NotNull List<Float> values, @NotNull LevelBasedValue fallback) implements LevelBasedValue {
        public static final Codec<Lookup> CODEC = StructCodec.struct(
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
        public @NotNull Codec<Lookup> codec() {
            return CODEC;
        }
    }

}
