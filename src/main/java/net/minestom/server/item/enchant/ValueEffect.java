package net.minestom.server.item.enchant;

import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed interface ValueEffect extends Enchantment.Effect {

    @NotNull BinaryTagSerializer<ValueEffect> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentValueEffects, ValueEffect::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends ValueEffect>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends ValueEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("add", Add.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("all_of", AllOf.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("multiply", Multiply.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("remove_binomial", RemoveBinomial.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("set", Set.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float apply(float base, int level);

    @NotNull BinaryTagSerializer<? extends ValueEffect> nbtType();

    record Add(@NotNull LevelBasedValue value) implements ValueEffect {
        public static final BinaryTagSerializer<Add> NBT_TYPE = BinaryTagSerializer.object("value", LevelBasedValue.NBT_TYPE, Add::value, Add::new);

        @Override
        public float apply(float base, int level) {
            return base + value.calc(level);
        }

        @Override
        public @NotNull BinaryTagSerializer<Add> nbtType() {
            return NBT_TYPE;
        }
    }

    record AllOf(@NotNull List<ValueEffect> effects) implements ValueEffect {
        public static final BinaryTagSerializer<AllOf> NBT_TYPE = BinaryTagSerializer.object("effects", ValueEffect.NBT_TYPE.list(), AllOf::effects, AllOf::new);

        public AllOf {
            effects = List.copyOf(effects);
        }

        @Override
        public float apply(float base, int level) {
            for (ValueEffect effect : effects)
                base = effect.apply(base, level);
            return base;
        }

        @Override
        public @NotNull BinaryTagSerializer<AllOf> nbtType() {
            return NBT_TYPE;
        }
    }

    record Multiply(@NotNull LevelBasedValue factor) implements ValueEffect {
        public static final BinaryTagSerializer<Multiply> NBT_TYPE = BinaryTagSerializer.object("factor", LevelBasedValue.NBT_TYPE, Multiply::factor, Multiply::new);

        @Override
        public float apply(float base, int level) {
            return base * factor.calc(level);
        }

        @Override
        public @NotNull BinaryTagSerializer<Multiply> nbtType() {
            return NBT_TYPE;
        }
    }

    record RemoveBinomial(@NotNull LevelBasedValue chance) implements ValueEffect {
        public static final BinaryTagSerializer<RemoveBinomial> NBT_TYPE = BinaryTagSerializer.object("chance", LevelBasedValue.NBT_TYPE, RemoveBinomial::chance, RemoveBinomial::new);

        @Override
        public float apply(float base, int level) {
            throw new UnsupportedOperationException("todo");
        }

        @Override
        public @NotNull BinaryTagSerializer<RemoveBinomial> nbtType() {
            return NBT_TYPE;
        }
    }

    record Set(@NotNull LevelBasedValue value) implements ValueEffect {
        public static final BinaryTagSerializer<Set> NBT_TYPE = BinaryTagSerializer.object("value", LevelBasedValue.NBT_TYPE, Set::value, Set::new);

        @Override
        public float apply(float base, int level) {
            return value.calc(level);
        }

        @Override
        public @NotNull BinaryTagSerializer<Set> nbtType() {
            return NBT_TYPE;
        }
    }

}
