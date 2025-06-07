package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed interface ValueEffect extends Enchantment.Effect {

    @NotNull StructCodec<ValueEffect> CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentValueEffects, ValueEffect::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends ValueEffect>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends ValueEffect>> registry = DynamicRegistry.create(Key.key("minestom:enchantment_value_effect"));
        registry.register("add", Add.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("all_of", AllOf.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("multiply", Multiply.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("remove_binomial", RemoveBinomial.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("set", Set.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float apply(float base, int level);

    @NotNull StructCodec<? extends ValueEffect> codec();

    record Add(@NotNull LevelBasedValue value) implements ValueEffect {
        public static final StructCodec<Add> CODEC = StructCodec.struct(
                "value", LevelBasedValue.CODEC, Add::value,
                Add::new);

        @Override
        public float apply(float base, int level) {
            return base + value.calc(level);
        }

        @Override
        public @NotNull StructCodec<Add> codec() {
            return CODEC;
        }
    }

    record AllOf(@NotNull List<ValueEffect> effects) implements ValueEffect {
        public static final StructCodec<AllOf> CODEC = StructCodec.struct(
                "effects", ValueEffect.CODEC.list(), AllOf::effects,
                AllOf::new);

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
        public @NotNull StructCodec<AllOf> codec() {
            return CODEC;
        }
    }

    record Multiply(@NotNull LevelBasedValue factor) implements ValueEffect {
        public static final StructCodec<Multiply> CODEC = StructCodec.struct(
                "factor", LevelBasedValue.CODEC, Multiply::factor,
                Multiply::new);

        @Override
        public float apply(float base, int level) {
            return base * factor.calc(level);
        }

        @Override
        public @NotNull StructCodec<Multiply> codec() {
            return CODEC;
        }
    }

    record RemoveBinomial(@NotNull LevelBasedValue chance) implements ValueEffect {
        public static final StructCodec<RemoveBinomial> CODEC = StructCodec.struct(
                "chance", LevelBasedValue.CODEC, RemoveBinomial::chance,
                RemoveBinomial::new);

        @Override
        public float apply(float base, int level) {
            throw new UnsupportedOperationException("todo");
        }

        @Override
        public @NotNull StructCodec<RemoveBinomial> codec() {
            return CODEC;
        }
    }

    record Set(@NotNull LevelBasedValue value) implements ValueEffect {
        public static final StructCodec<Set> CODEC = StructCodec.struct("value", LevelBasedValue.CODEC, Set::value, Set::new);

        @Override
        public float apply(float base, int level) {
            return value.calc(level);
        }

        @Override
        public @NotNull StructCodec<Set> codec() {
            return CODEC;
        }
    }

}
