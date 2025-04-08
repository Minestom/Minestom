package net.minestom.server.config;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public interface FloatProvider {

    @NotNull StructCodec<FloatProvider> TAGGED_CODEC = Codec.RegistryTaggedUnion(
            Registries::floatProvider, FloatProvider::codec, "type");
    @NotNull Codec<FloatProvider> CODEC = new Codec<>() {
        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable FloatProvider value) {
            if (value instanceof FloatProvider.Constant(float constantValue))
                return new Result.Ok<>(coder.createFloat(constantValue));
            return TAGGED_CODEC.encode(coder, value);
        }

        @Override
        public @NotNull <D> Result<FloatProvider> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Float> numberResult = coder.getFloat(value);
            if (numberResult instanceof Result.Ok(Float number))
                return new Result.Ok<>(new FloatProvider.Constant(number));
            return TAGGED_CODEC.decode(coder, value);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends FloatProvider>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends FloatProvider>> registry = DynamicRegistry.create("minestom:float_provider");
        registry.register("constant", FloatProvider.Constant.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("uniform", FloatProvider.Uniform.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("clamped_normal", FloatProvider.ClampedNormal.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("trapezoid", FloatProvider.Trapezoid.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float sample(Random random);

    @NotNull StructCodec<? extends FloatProvider> codec();

    record Constant(float value) implements FloatProvider {
        public static final StructCodec<Constant> CODEC = StructCodec.struct(
                "value", Codec.FLOAT, Constant::value,
                Constant::new
        );

        @Override
        public float sample(Random random) {
            return value;
        }

        @Override
        public @NotNull StructCodec<Constant> codec() {
            return CODEC;
        }
    }

    record Uniform(float minInclusive, float maxExclusive) implements FloatProvider {
        public static final StructCodec<Uniform> CODEC = StructCodec.struct(
                "min_inclusive", Codec.FLOAT, Uniform::minInclusive,
                "max_exclusive", Codec.FLOAT, Uniform::maxExclusive,
                Uniform::new
        );

        @Override
        public float sample(Random random) {
            return random.nextFloat() * (maxExclusive - minInclusive) + minInclusive;
        }

        @Override
        public @NotNull StructCodec<Uniform> codec() {
            return CODEC;
        }
    }

    record ClampedNormal(float mean, float deviation, float min, float max) implements FloatProvider {
        public static final StructCodec<ClampedNormal> NBT_TYPE = StructCodec.struct(
                "mean", Codec.FLOAT, ClampedNormal::mean,
                "deviation", Codec.FLOAT, ClampedNormal::deviation,
                "min", Codec.FLOAT, ClampedNormal::min,
                "max", Codec.FLOAT, ClampedNormal::max,
                ClampedNormal::new
        );

        @Override
        public float sample(Random random) {
            return Math.min(max, Math.max(min, mean + (float) random.nextGaussian() * deviation));
        }

        @Override
        public @NotNull StructCodec<? extends FloatProvider> codec() {
            return NBT_TYPE;
        }
    }

    record Trapezoid(float min, float max, float plateau) implements FloatProvider {
        public static final StructCodec<Trapezoid> NBT_TYPE = StructCodec.struct(
                "min", Codec.FLOAT, Trapezoid::min,
                "max", Codec.FLOAT, Trapezoid::max,
                "plateau", Codec.FLOAT, Trapezoid::plateau,
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
        public @NotNull StructCodec<? extends FloatProvider> codec() {
            return NBT_TYPE;
        }
    }
}
