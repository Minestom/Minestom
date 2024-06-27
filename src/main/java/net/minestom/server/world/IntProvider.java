package net.minestom.server.world;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

/**
 * Represents the Int Provider for monster_spawn_light_level for <a href="https://minecraft.wiki/w/Dimension_type">Dimension Type</a>
 */
public interface IntProvider {

    int get(@NotNull Random random);

    int getMin();

    int getMax();

    @NotNull
    BinaryTag toBinaryTag();

    record ConstantIntProvider(int constant) implements IntProvider {

        @Override
        public int get(@NotNull Random random) {
            return constant;
        }

        @Override
        public int getMin() {
            return constant;
        }

        @Override
        public int getMax() {
            return constant;
        }

        @Override
        public @NotNull BinaryTag toBinaryTag() {
            return CompoundBinaryTag.builder().putString("type", "minecraft:constant").putInt("value", constant).build();
        }
    }

    record UniformIntProvider(int min, int max) implements IntProvider {

        @Override
        public int get(@NotNull Random random) {
            return Math.round(random.nextFloat() * (max - min)) + min;
        }

        @Override
        public int getMin() {
            return min;
        }

        @Override
        public int getMax() {
            return max;
        }

        @Override
        public @NotNull BinaryTag toBinaryTag() {
            return CompoundBinaryTag.builder().putString("type", "minecraft:uniform").putInt("min_inclusive", min).putInt("max_inclusive", max).build();
        }
    }

    static @NotNull IntProvider createIntProvider(int constant) {
        return new ConstantIntProvider(constant);
    }

    static @NotNull IntProvider createIntProvider(@NotNull Object properties) {
        if (properties instanceof Long longNum) {
            return new ConstantIntProvider(longNum.intValue());
        }
        if (properties instanceof HashMap<?,?> hashMap) {
            String type = (String) hashMap.get("type");
            switch (type) {
                case "minecraft:uniform" -> {
                    // Have to cast them to longs since they are stored as longs
                    Long min = (Long) hashMap.get("min_inclusive");
                    Long max = (Long) hashMap.get("max_inclusive");
                    return new UniformIntProvider(min.intValue(), max.intValue());
                }
                case "minecraft:constant" -> {
                    return new ConstantIntProvider(((Long) hashMap.get("value")).intValue());
                }
                default -> throw new IllegalArgumentException("Unknown IntProvider type " + type);
            }
            // TODO Biased to bottom, clamped, clamped normal, weighed list
        }
        return new ConstantIntProvider(0);
    }
}
