package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static net.kyori.adventure.nbt.FloatBinaryTag.floatBinaryTag;

public interface FloatProvider {

    @NotNull BinaryTagSerializer<FloatProvider> TAGGED_NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentFloatProviders, FloatProvider::nbtType, "type");
    @NotNull BinaryTagSerializer<FloatProvider> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull FloatProvider value) {
            if (value instanceof FloatProvider.Constant constant) return floatBinaryTag(constant.value);
            return TAGGED_NBT_TYPE.write(context, value);
        }

        @Override
        public @NotNull FloatProvider read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (tag instanceof NumberBinaryTag number) return new FloatProvider.Constant(number.floatValue());
            return TAGGED_NBT_TYPE.read(context, tag);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends FloatProvider>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends FloatProvider>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("constant", FloatProvider.Constant.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("uniform", FloatProvider.Uniform.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("clamped_normal", FloatProvider.ClampedNormal.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("trapezoid", FloatProvider.Trapezoid.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float sample(Random random);

    @NotNull BinaryTagSerializer<? extends FloatProvider> nbtType();

    record Constant(float value) implements FloatProvider {
        public static final BinaryTagSerializer<Constant> NBT_TYPE = BinaryTagSerializer.object(
                "value", BinaryTagSerializer.FLOAT, Constant::value,
                Constant::new
        );

        @Override
        public float sample(Random random) {
            return value;
        }

        @Override
        public @NotNull BinaryTagSerializer<Constant> nbtType() {
            throw new UnsupportedOperationException("Constant values are serialized as a special case, see FloatProvider.NBT_TYPE");
        }
    }

    record Uniform(float minInclusive, float maxExclusive) implements FloatProvider {
        public static final BinaryTagSerializer<Uniform> NBT_TYPE = BinaryTagSerializer.object(
                "min_inclusive", BinaryTagSerializer.FLOAT, Uniform::minInclusive,
                "max_exclusive", BinaryTagSerializer.FLOAT, Uniform::maxExclusive,
                Uniform::new
        );

        @Override
        public float sample(Random random) {
            return random.nextFloat() * (maxExclusive - minInclusive) + minInclusive;
        }

        @Override
        public @NotNull BinaryTagSerializer<Uniform> nbtType() {
            return NBT_TYPE;
        }
    }

    record ClampedNormal(float mean, float deviation, float min, float max) implements FloatProvider {
        public static final BinaryTagSerializer<ClampedNormal> NBT_TYPE = BinaryTagSerializer.object(
                "mean", BinaryTagSerializer.FLOAT, ClampedNormal::mean,
                "deviation", BinaryTagSerializer.FLOAT, ClampedNormal::deviation,
                "min", BinaryTagSerializer.FLOAT, ClampedNormal::min,
                "max", BinaryTagSerializer.FLOAT, ClampedNormal::max,
                ClampedNormal::new
        );

        @Override
        public float sample(Random random) {
            return Math.min(max, Math.max(min, mean + (float) random.nextGaussian() * deviation));
        }

        @Override
        public @NotNull BinaryTagSerializer<? extends FloatProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record Trapezoid(float min, float max, float plateau) implements FloatProvider {
        public static final BinaryTagSerializer<Trapezoid> NBT_TYPE = BinaryTagSerializer.object(
                "min", BinaryTagSerializer.FLOAT, Trapezoid::min,
                "max", BinaryTagSerializer.FLOAT, Trapezoid::max,
                "plateau", BinaryTagSerializer.FLOAT, Trapezoid::plateau,
                Trapezoid::new
        );

        @Override
        public float sample(Random random) {
            float range = max - min;
            float upper = (range - plateau) / 2.0f;
            float lower = range - upper;
            return min + random.nextFloat() * lower + random.nextFloat() * upper;
        }

        @Override
        public @NotNull BinaryTagSerializer<? extends FloatProvider> nbtType() {
            return NBT_TYPE;
        }
    }
}
