package net.minestom.server.config;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static net.kyori.adventure.nbt.IntBinaryTag.intBinaryTag;

public interface IntProvider {

    @NotNull BinaryTagSerializer<IntProvider> TAGGED_NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::intProviders, IntProvider::nbtType, "type");
    @NotNull BinaryTagSerializer<IntProvider> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull IntProvider value) {
            if (value instanceof IntProvider.Constant constant) return intBinaryTag(constant.value);
            return TAGGED_NBT_TYPE.write(context, value);
        }

        @Override
        public @NotNull IntProvider read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (tag instanceof NumberBinaryTag number) return new IntProvider.Constant(number.intValue());
            return TAGGED_NBT_TYPE.read(context, tag);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends IntProvider>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends IntProvider>> registry = DynamicRegistry.create("minestom:int_provider");
        registry.register("constant", IntProvider.Constant.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("uniform", IntProvider.Uniform.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("biased_to_bottom", IntProvider.BiasedToBottom.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("clamped", IntProvider.Clamped.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("clamped_normal", IntProvider.ClampedNormal.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("weighted_list", IntProvider.WeightedList.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    int sample(Random random);

    @NotNull BinaryTagSerializer<? extends IntProvider> nbtType();

    record Constant(int value) implements IntProvider {
        public static final BinaryTagSerializer<Constant> NBT_TYPE = BinaryTagSerializer.object(
                "value", BinaryTagSerializer.INT, Constant::value,
                Constant::new
        );

        @Override
        public int sample(Random random) {
            return value;
        }

        @Override
        public @NotNull BinaryTagSerializer<Constant> nbtType() {
            return NBT_TYPE;
        }
    }

    record Uniform(int minInclusive, int maxInclusive) implements IntProvider {
        public static final BinaryTagSerializer<Uniform> NBT_TYPE = BinaryTagSerializer.object(
                "min_inclusive", BinaryTagSerializer.INT, Uniform::minInclusive,
                "max_inclusive", BinaryTagSerializer.INT, Uniform::maxInclusive,
                Uniform::new
        );

        @Override
        public int sample(Random random) {
            return random.nextInt(minInclusive, minInclusive + 1);
        }

        @Override
        public @NotNull BinaryTagSerializer<Uniform> nbtType() {
            return NBT_TYPE;
        }
    }

    record BiasedToBottom(int minInclusive, int maxInclusive) implements IntProvider {
        public static final BinaryTagSerializer<BiasedToBottom> NBT_TYPE = BinaryTagSerializer.object(
                "min_inclusive", BinaryTagSerializer.INT, BiasedToBottom::minInclusive,
                "max_inclusive", BinaryTagSerializer.INT, BiasedToBottom::maxInclusive,
                BiasedToBottom::new
        );

        @Override
        public int sample(Random random) {
            return minInclusive + random.nextInt(random.nextInt(maxInclusive - minInclusive + 1) + 1);
        }

        @Override
        public @NotNull BinaryTagSerializer<? extends IntProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record Clamped(int minInclusive, int maxInclusive, IntProvider source) implements IntProvider {
        public static final BinaryTagSerializer<Clamped> NBT_TYPE = BinaryTagSerializer.object(
                "min_inclusive", BinaryTagSerializer.INT, Clamped::minInclusive,
                "max_inclusive", BinaryTagSerializer.INT, Clamped::maxInclusive,
                "source", IntProvider.NBT_TYPE, Clamped::source,
                Clamped::new
        );

        @Override
        public int sample(Random random) {
            return Math.min(maxInclusive, Math.max(minInclusive, source.sample(random)));
        }

        @Override
        public @NotNull BinaryTagSerializer<? extends IntProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record ClampedNormal(float mean, float deviation, int minInclusive, int maxInclusive) implements IntProvider {
        public static final BinaryTagSerializer<ClampedNormal> NBT_TYPE = BinaryTagSerializer.object(
                "mean", BinaryTagSerializer.FLOAT, ClampedNormal::mean,
                "deviation", BinaryTagSerializer.FLOAT, ClampedNormal::deviation,
                "min_inclusive", BinaryTagSerializer.INT, ClampedNormal::minInclusive,
                "max_inclusive", BinaryTagSerializer.INT, ClampedNormal::maxInclusive,
                ClampedNormal::new
        );

        @Override
        public int sample(Random random) {
            return (int) Math.min((float) maxInclusive, Math.max((float) minInclusive, (mean + (float) random.nextGaussian() * deviation)));
        }

        @Override
        public @NotNull BinaryTagSerializer<? extends IntProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record WeightedList(@NotNull List<Entry> distribution) implements IntProvider {
        public static final BinaryTagSerializer<WeightedList> NBT_TYPE = BinaryTagSerializer.object(
                "distribution", Entry.NBT_TYPE.list(), WeightedList::distribution,
                WeightedList::new
        );

        @Override
        public int sample(Random random) {
            int totalWeight = 0;
            for (Entry entry : distribution) {
                totalWeight += entry.weight;
            }

            int value = random.nextInt(totalWeight);
            for (Entry entry : distribution) {
                value -= entry.weight;
                if (value < 0) {
                    return entry.data().sample(random);
                }
            }

            return distribution.getFirst().data().sample(random);
        }

        @Override
        public @NotNull BinaryTagSerializer<? extends IntProvider> nbtType() {
            return NBT_TYPE;
        }

        public record Entry(@NotNull IntProvider data, int weight) {
            public static final BinaryTagSerializer<Entry> NBT_TYPE = BinaryTagSerializer.object(
                    "data", IntProvider.NBT_TYPE, Entry::data,
                    "weight", BinaryTagSerializer.INT, Entry::weight,
                    Entry::new
            );
        }
    }
}
