package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
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

    @NotNull BinaryTagSerializer<LevelBasedValue> TAGGED_NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentLevelBasedValues, LevelBasedValue::nbtType, "type");
    @NotNull BinaryTagSerializer<LevelBasedValue> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull LevelBasedValue value) {
            if (value instanceof Constant constant) return floatBinaryTag(constant.value);
            return TAGGED_NBT_TYPE.write(context, value);
        }

        @Override
        public @NotNull LevelBasedValue read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (tag instanceof NumberBinaryTag number) return new Constant(number.floatValue());
            return TAGGED_NBT_TYPE.read(context, tag);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends LevelBasedValue>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends LevelBasedValue>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        // Note that constant is omitted from the registry, it has serialization handled out of band above.
        registry.register("linear", Linear.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("clamped", Clamped.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("fraction", Fraction.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("levels_squared", LevelsSquared.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("lookup", Lookup.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float calc(int level);

    @NotNull BinaryTagSerializer<? extends LevelBasedValue> nbtType();

    record Constant(float value) implements LevelBasedValue {

        @Override
        public float calc(int level) {
            return value;
        }

        @Override
        public @NotNull BinaryTagSerializer<Constant> nbtType() {
            throw new UnsupportedOperationException("Constant values are serialized as a special case, see LevelBasedValue.NBT_TYPE");
        }
    }

    record Linear(float base, float perLevelAboveFirst) implements LevelBasedValue {
        public static final BinaryTagSerializer<Linear> NBT_TYPE = BinaryTagSerializer.object(
                "base", BinaryTagSerializer.FLOAT, Linear::base,
                "per_level_above_first", BinaryTagSerializer.FLOAT, Linear::perLevelAboveFirst,
                Linear::new
        );

        @Override
        public float calc(int level) {
            return base + (perLevelAboveFirst * (level - 1));
        }

        @Override
        public @NotNull BinaryTagSerializer<Linear> nbtType() {
            return NBT_TYPE;
        }
    }

    record Clamped(@NotNull LevelBasedValue value, float min, float max) implements LevelBasedValue {
        public static final BinaryTagSerializer<Clamped> NBT_TYPE = BinaryTagSerializer.object(
                "value", LevelBasedValue.NBT_TYPE, Clamped::value,
                "min", BinaryTagSerializer.FLOAT, Clamped::min,
                "max", BinaryTagSerializer.FLOAT, Clamped::max,
                Clamped::new
        );

        @Override
        public float calc(int level) {
            return MathUtils.clamp(value.calc(level), min, max);
        }

        @Override
        public @NotNull BinaryTagSerializer<Clamped> nbtType() {
            return NBT_TYPE;
        }
    }

    record Fraction(@NotNull LevelBasedValue numerator, @NotNull LevelBasedValue denominator) implements LevelBasedValue {
        public static final BinaryTagSerializer<Fraction> NBT_TYPE = BinaryTagSerializer.object(
                "numerator", LevelBasedValue.NBT_TYPE, Fraction::numerator,
                "denominator", LevelBasedValue.NBT_TYPE, Fraction::denominator,
                Fraction::new
        );

        @Override
        public float calc(int level) {
            float denominator = this.denominator.calc(level);
            return denominator == 0f ? 0f : numerator.calc(level) / denominator;
        }

        @Override
        public @NotNull BinaryTagSerializer<Fraction> nbtType() {
            return NBT_TYPE;
        }
    }

    record LevelsSquared(float added) implements LevelBasedValue {
        public static final BinaryTagSerializer<LevelsSquared> NBT_TYPE = BinaryTagSerializer.object(
                "added", BinaryTagSerializer.FLOAT, LevelsSquared::added,
                LevelsSquared::new
        );

        @Override
        public float calc(int level) {
            return (level * level) + added;
        }

        @Override
        public @NotNull BinaryTagSerializer<LevelsSquared> nbtType() {
            return NBT_TYPE;
        }
    }

    record Lookup(@NotNull List<Float> values, @NotNull LevelBasedValue fallback) implements LevelBasedValue {
        public static final BinaryTagSerializer<Lookup> NBT_TYPE = BinaryTagSerializer.object(
                "values", BinaryTagSerializer.FLOAT.list(), Lookup::values,
                "fallback", LevelBasedValue.NBT_TYPE, Lookup::fallback,
                Lookup::new
        );

        @Override
        public float calc(int level) {
            if (level < 0 || level > values.size()) return fallback.calc(level);
            return values.get(level - 1);
        }

        @Override
        public @NotNull BinaryTagSerializer<Lookup> nbtType() {
            return NBT_TYPE;
        }
    }

}
