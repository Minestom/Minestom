package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed interface ValueEffect extends Enchantment.Effect {

    @NotNull Codec<ValueEffect> CODEC = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentValueEffects, ValueEffect::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Codec<? extends ValueEffect>> createDefaultRegistry() {
        final DynamicRegistry<Codec<? extends ValueEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("add", Add.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("all_of", AllOf.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("multiply", Multiply.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("remove_binomial", RemoveBinomial.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("set", Set.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    float apply(float base, int level);

    @NotNull Codec<? extends ValueEffect> codec();

    record Add(@NotNull LevelBasedValue value) implements ValueEffect {
        public static final Codec<Add> CODEC = StructCodec.struct(
                "value", LevelBasedValue.CODEC, Add::value,
                Add::new);

        @Override
        public float apply(float base, int level) {
            return base + value.calc(level);
        }

        @Override
        public @NotNull Codec<Add> codec() {
            return CODEC;
        }
    }

    record AllOf(@NotNull List<ValueEffect> effects) implements ValueEffect {
        public static final Codec<AllOf> CODEC = StructCodec.struct(
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
        public @NotNull Codec<AllOf> codec() {
            return CODEC;
        }
    }

    record Multiply(@NotNull LevelBasedValue factor) implements ValueEffect {
        public static final Codec<Multiply> CODEC = StructCodec.struct(
                "factor", LevelBasedValue.CODEC, Multiply::factor,
                Multiply::new);

        @Override
        public float apply(float base, int level) {
            return base * factor.calc(level);
        }

        @Override
        public @NotNull Codec<Multiply> codec() {
            return CODEC;
        }
    }

    record RemoveBinomial(@NotNull LevelBasedValue chance) implements ValueEffect {
        public static final Codec<RemoveBinomial> CODEC = StructCodec.struct(
                "chance", LevelBasedValue.CODEC, RemoveBinomial::chance,
                RemoveBinomial::new);

        @Override
        public float apply(float base, int level) {
            throw new UnsupportedOperationException("todo");
        }

        @Override
        public @NotNull Codec<RemoveBinomial> codec() {
            return CODEC;
        }
    }

    record Set(@NotNull LevelBasedValue value) implements ValueEffect {
        public static final Codec<Set> CODEC = StructCodec.struct("value", LevelBasedValue.CODEC, Set::value, Set::new);

        @Override
        public float apply(float base, int level) {
            return value.calc(level);
        }

        @Override
        public @NotNull Codec<Set> codec() {
            return CODEC;
        }
    }

}
