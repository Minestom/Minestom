package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public non-sealed interface EntityEffect extends Enchantment.Effect {

    @NotNull StructCodec<EntityEffect> CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentEntityEffects, EntityEffect::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends EntityEffect>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends EntityEffect>> registry = DynamicRegistry.create(RegistryKey.unsafeOf("minestom:enchantment_entity_effect"));
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
            @NotNull RegistryTag<PotionEffect> toApply,
            @NotNull LevelBasedValue minDuration,
            @NotNull LevelBasedValue maxDuration,
            @NotNull LevelBasedValue minAmplifier,
            @NotNull LevelBasedValue maxAmplifier
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<ApplyPotionEffect> CODEC = StructCodec.struct(
                "to_apply", RegistryTag.codec(Registries::potionEffect), ApplyPotionEffect::toApply,
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
            @NotNull RegistryKey<DamageType> damageType,
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

    record Explode(
            boolean attributeToUser,
            @Nullable RegistryKey<DamageType> damageType,
            @Nullable LevelBasedValue knockbackMultiplier,
            @Nullable Codec.RawValue immuneBlocks,
            @NotNull Point offset,
            @NotNull LevelBasedValue radius,
            boolean createFire,
            @NotNull Codec.RawValue blockInteraction,
            @NotNull Codec.RawValue smallParticle,
            @NotNull Codec.RawValue largeParticle,
            @NotNull SoundEvent sound
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<Explode> CODEC = StructCodec.struct(
                "attribute_to_user", Codec.BOOLEAN.optional(false), Explode::attributeToUser,
                "damage_type", DamageType.CODEC.optional(), Explode::damageType,
                "knockback_multiplier", LevelBasedValue.CODEC.optional(), Explode::knockbackMultiplier,
                "immune_blocks", Codec.RAW_VALUE.optional(), Explode::immuneBlocks,
                "offset", Codec.VECTOR3D.optional(Vec.ZERO), Explode::offset,
                "radius", LevelBasedValue.CODEC, Explode::radius,
                "create_fire", Codec.BOOLEAN.optional(false), Explode::createFire,
                "block_interaction", Codec.RAW_VALUE, Explode::blockInteraction,
                "small_particle", Codec.RAW_VALUE, Explode::smallParticle,
                "large_particle", Codec.RAW_VALUE, Explode::largeParticle,
                "sound", SoundEvent.CODEC, Explode::sound,
                Explode::new);

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

    record PlaySound(
            @NotNull SoundEvent soundEvent,
            @NotNull Codec.RawValue volume,
            @NotNull Codec.RawValue pitch
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<PlaySound> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, PlaySound::soundEvent,
                "volume", Codec.RAW_VALUE, PlaySound::volume,
                "pitch", Codec.RAW_VALUE, PlaySound::pitch,
                PlaySound::new);

        @Override
        public @NotNull StructCodec<PlaySound> codec() {
            return CODEC;
        }
    }

    record ReplaceBlock(
            @NotNull Codec.RawValue offset,
            @Nullable Codec.RawValue predicate,
            @NotNull Codec.RawValue blockState,
            @Nullable Codec.RawValue triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<ReplaceBlock> CODEC = StructCodec.struct(
                "offset", Codec.RAW_VALUE, ReplaceBlock::offset,
                "predicate", Codec.RAW_VALUE, ReplaceBlock::predicate,
                "block_state", Codec.RAW_VALUE, ReplaceBlock::blockState,
                "trigger_game_event", Codec.RAW_VALUE, ReplaceBlock::triggerGameEvent,
                ReplaceBlock::new);

        @Override
        public @NotNull StructCodec<ReplaceBlock> codec() {
            return CODEC;
        }
    }

    record ReplaceDisc(
            @NotNull LevelBasedValue radius,
            @NotNull LevelBasedValue height,
            @NotNull Codec.RawValue offset,
            @Nullable Codec.RawValue predicate,
            @NotNull Codec.RawValue blockState,
            @Nullable Codec.RawValue triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<ReplaceDisc> CODEC = StructCodec.struct(
                "radius", LevelBasedValue.CODEC, ReplaceDisc::radius,
                "height", LevelBasedValue.CODEC, ReplaceDisc::height,
                "offset", Codec.RAW_VALUE, ReplaceDisc::offset,
                "predicate", Codec.RAW_VALUE.optional(), ReplaceDisc::predicate,
                "block_state", Codec.RAW_VALUE, ReplaceDisc::blockState,
                "trigger_game_event", Codec.RAW_VALUE.optional(), ReplaceDisc::triggerGameEvent,
                ReplaceDisc::new);

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

    record SetBlockProperties(
            @NotNull Codec.RawValue properties,
            @NotNull Codec.RawValue offset,
            @Nullable Codec.RawValue triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<SetBlockProperties> CODEC = StructCodec.struct(
                "properties", Codec.RAW_VALUE, SetBlockProperties::properties,
                "offset", Codec.RAW_VALUE, SetBlockProperties::offset,
                "trigger_game_event", Codec.RAW_VALUE.optional(), SetBlockProperties::triggerGameEvent,
                SetBlockProperties::new);

        @Override
        public @NotNull StructCodec<SetBlockProperties> codec() {
            return CODEC;
        }
    }

    record SpawnParticles(
            @NotNull Codec.RawValue particle,
            @NotNull Codec.RawValue horizontalPosition,
            @NotNull Codec.RawValue verticalPosition,
            @NotNull Codec.RawValue horizontalVelocity,
            @NotNull Codec.RawValue verticalVelocity,
            @NotNull Codec.RawValue speed
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<SpawnParticles> CODEC = StructCodec.struct(
                "particle", Codec.RAW_VALUE, SpawnParticles::particle,
                "horizontal_position", Codec.RAW_VALUE, SpawnParticles::horizontalPosition,
                "vertical_position", Codec.RAW_VALUE, SpawnParticles::verticalPosition,
                "horizontal_velocity", Codec.RAW_VALUE, SpawnParticles::horizontalVelocity,
                "vertical_velocity", Codec.RAW_VALUE, SpawnParticles::verticalVelocity,
                "speed", Codec.RAW_VALUE, SpawnParticles::speed,
                SpawnParticles::new);

        @Override
        public @NotNull StructCodec<SpawnParticles> codec() {
            return CODEC;
        }
    }

    record SummonEntity(
            @NotNull Codec.RawValue entityTypes,
            boolean joinTeam
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<SummonEntity> CODEC = StructCodec.struct(
                "entity", Codec.RAW_VALUE, SummonEntity::entityTypes,
                "join_team", Codec.BOOLEAN.optional(false), SummonEntity::joinTeam,
                SummonEntity::new);

        @Override
        public @NotNull StructCodec<SummonEntity> codec() {
            return CODEC;
        }
    }

}
