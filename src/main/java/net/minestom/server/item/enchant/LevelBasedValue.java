package net.minestom.server.item.enchant;

import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

public interface LevelBasedValue {

    float calc(int level);

    record Constant(float value) implements LevelBasedValue {
        @Override
        public float calc(int level) {
            return value;
        }
    }

    record Linear(int base, int perLevelAboveFirst) implements LevelBasedValue {
        @Override
        public float calc(int level) {
            return base + (perLevelAboveFirst * (level - 1));
        }
    }

    record Clamped(@NotNull LevelBasedValue value, float min, float max) implements LevelBasedValue {
        @Override
        public float calc(int level) {
            return MathUtils.clamp(value.calc(level), min, max);
        }
    }

    record Fraction(@NotNull LevelBasedValue numerator, @NotNull LevelBasedValue denominator) implements LevelBasedValue {
        @Override
        public float calc(int level) {
            float denominator = this.denominator.calc(level);
            return denominator == 0f ? 0f : numerator.calc(level) / denominator;
        }
    }

    record LevelsSquared(float added) implements LevelBasedValue {
        @Override
        public float calc(int level) {
            return (level * level) + added;
        }
    }

}
