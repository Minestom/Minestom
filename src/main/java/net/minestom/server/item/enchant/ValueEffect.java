package net.minestom.server.item.enchant;

import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed interface ValueEffect extends Enchantment.Effect {

    @NotNull BinaryTagSerializer<ValueEffect> NBT_TYPE = null; //todo

    float apply(float base, int level);

    record Add(@NotNull LevelBasedValue value) implements ValueEffect {
        @Override
        public float apply(float base, int level) {
            return base + value.calc(level);
        }
    }

    record AllOf(@NotNull List<ValueEffect> effects) implements ValueEffect {
        public AllOf {
            effects = List.copyOf(effects);
        }

        @Override
        public float apply(float base, int level) {
            for (ValueEffect effect : effects)
                base = effect.apply(base, level);
            return base;
        }
    }

    record Multiply(@NotNull LevelBasedValue value) implements ValueEffect {
        @Override
        public float apply(float base, int level) {
            return base * value.calc(level);
        }
    }

    record RemoveBinomial(@NotNull LevelBasedValue chance) implements ValueEffect {
        @Override
        public float apply(float base, int level) {
            throw new UnsupportedOperationException("todo");
        }
    }

    record Set(@NotNull LevelBasedValue value) implements ValueEffect {
        @Override
        public float apply(float base, int level) {
            return value.calc(level);
        }
    }

}
