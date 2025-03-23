package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public non-sealed interface EntityEffect extends Enchantment.Effect {

    @NotNull StructCodec<EntityEffect> CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentEntityEffects, EntityEffect::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends EntityEffect>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends EntityEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("all_of", AllOf.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("apply_mob_effect", ApplyPotionEffect.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("change_item_damage", ChangeItemDamage.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("damage_entity", DamageEntity.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("explode", Explode.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("ignite", Ignite.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("play_sound", PlaySound.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("replace_block", ReplaceBlock.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("replace_disk", ReplaceDisc.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("run_function", RunFunction.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("set_block_properties", SetBlockProperties.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("spawn_particles", SpawnParticles.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("summon_entity", SummonEntity.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    @NotNull StructCodec<? extends EntityEffect> codec();

    record AllOf(@NotNull List<EntityEffect> effect) implements EntityEffect {
        public static final StructCodec<AllOf> CODEC = StructCodec.struct(
                "effects", EntityEffect.CODEC.list(), AllOf::effect,
                AllOf::new);

        public AllOf {
            effect = List.copyOf(effect);
        }

        @Override
        public @NotNull StructCodec<AllOf> codec() {
            return CODEC;
        }
    }

    record ApplyPotionEffect(
            @NotNull ObjectSet<PotionEffect> toApply,
            @NotNull LevelBasedValue minDuration,
            @NotNull LevelBasedValue maxDuration,
            @NotNull LevelBasedValue minAmplifier,
            @NotNull LevelBasedValue maxAmplifier
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<ApplyPotionEffect> CODEC = StructCodec.struct(
                "to_apply", ObjectSet.codec(Tag.BasicType.POTION_EFFECTS), ApplyPotionEffect::toApply,
                "min_duration", LevelBasedValue.CODEC, ApplyPotionEffect::minDuration,
                "max_duration", LevelBasedValue.CODEC, ApplyPotionEffect::maxDuration,
                "min_amplifier", LevelBasedValue.CODEC, ApplyPotionEffect::minAmplifier,
                "max_amplifier", LevelBasedValue.CODEC, ApplyPotionEffect::maxAmplifier,
                ApplyPotionEffect::new
        );

        @Override
        public @NotNull StructCodec<ApplyPotionEffect> codec() {
            return CODEC;
        }
    }

    record DamageEntity(
            @NotNull DynamicRegistry.Key<DamageType> damageType,
            @NotNull LevelBasedValue minDamage,
            @NotNull LevelBasedValue maxDamage
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<DamageEntity> CODEC = StructCodec.struct(
                "damage_type", DamageType.CODEC, DamageEntity::damageType,
                "min_damage", LevelBasedValue.CODEC, DamageEntity::minDamage,
                "max_damage", LevelBasedValue.CODEC, DamageEntity::maxDamage,
                DamageEntity::new);

        @Override
        public @NotNull StructCodec<DamageEntity> codec() {
            return CODEC;
        }
    }

    record ChangeItemDamage(@NotNull LevelBasedValue amount) implements EntityEffect, LocationEffect {
        public static final StructCodec<ChangeItemDamage> CODEC = StructCodec.struct(
                "amount", LevelBasedValue.CODEC, ChangeItemDamage::amount,
                ChangeItemDamage::new
        );

        @Override
        public @NotNull StructCodec<ChangeItemDamage> codec() {
            return CODEC;
        }
    }

    record Explode(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<Explode> CODEC = StructCodec.struct(Explode::new);

        @Override
        public @NotNull StructCodec<Explode> codec() {
            return CODEC;
        }
    }

    record Ignite(@NotNull LevelBasedValue duration) implements EntityEffect, LocationEffect {
        public static final StructCodec<Ignite> CODEC = StructCodec.struct(
                "duration", LevelBasedValue.CODEC, Ignite::duration,
                Ignite::new
        );

        @Override
        public @NotNull StructCodec<Ignite> codec() {
            return CODEC;
        }
    }

    record PlaySound(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<PlaySound> CODEC = StructCodec.struct(PlaySound::new);

        @Override
        public @NotNull StructCodec<PlaySound> codec() {
            return CODEC;
        }
    }

    record ReplaceBlock(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<ReplaceBlock> CODEC = StructCodec.struct(ReplaceBlock::new);

        @Override
        public @NotNull StructCodec<ReplaceBlock> codec() {
            return CODEC;
        }
    }

    record ReplaceDisc(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<ReplaceDisc> CODEC = StructCodec.struct(ReplaceDisc::new);

        @Override
        public @NotNull StructCodec<ReplaceDisc> codec() {
            return CODEC;
        }
    }

    record RunFunction(
            @NotNull String function
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<RunFunction> CODEC = StructCodec.struct(
                "function", Codec.STRING, RunFunction::function,
                RunFunction::new);

        @Override
        public @NotNull StructCodec<RunFunction> codec() {
            return CODEC;
        }
    }

    record SetBlockProperties(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<SetBlockProperties> CODEC = StructCodec.struct(SetBlockProperties::new);

        @Override
        public @NotNull StructCodec<SetBlockProperties> codec() {
            return CODEC;
        }
    }

    record SpawnParticles(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<SpawnParticles> CODEC = StructCodec.struct(SpawnParticles::new);

        @Override
        public @NotNull StructCodec<SpawnParticles> codec() {
            return CODEC;
        }
    }

    record SummonEntity(/* todo */) implements EntityEffect, LocationEffect {
        public static final StructCodec<SummonEntity> CODEC = StructCodec.struct(SummonEntity::new);

        @Override
        public @NotNull StructCodec<SummonEntity> codec() {
            return CODEC;
        }
    }

}
