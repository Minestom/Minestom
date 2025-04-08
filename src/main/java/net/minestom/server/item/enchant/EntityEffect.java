package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.config.BlockPredicate;
import net.minestom.server.config.BlockStateProvider;
import net.minestom.server.config.FloatProvider;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.component.ItemBlockState;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
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
            @NotNull ObjectSet toApply,
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
                ApplyPotionEffect::new);

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
                ChangeItemDamage::new);

        @Override
        public @NotNull StructCodec<ChangeItemDamage> codec() {
            return CODEC;
        }
    }

    record Explode(
            boolean attributeToUser,
            @Nullable DynamicRegistry.Key<DamageType> damageType,
            @Nullable ObjectSet immuneBlocks,
            @Nullable LevelBasedValue knockbackMultiplier,
            @Nullable Point offset,
            @NotNull LevelBasedValue radius,
            boolean createFire,
            @NotNull BlockInteraction blockInteraction,
            @NotNull Particle smallParticle,
            @NotNull Particle largeParticle,
            @NotNull SoundEvent sound
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<Explode> CODEC = StructCodec.struct(
                "attribute_to_user", Codec.BOOLEAN, Explode::attributeToUser,
                "damage_type", DamageType.CODEC.optional(), Explode::damageType,
                "immune_blocks", ObjectSet.codec(Tag.BasicType.BLOCKS).optional(), Explode::immuneBlocks,
                "knockback_multiplier", LevelBasedValue.CODEC.optional(), Explode::knockbackMultiplier,
                "offset", Codec.BLOCK_POSITION.optional(), Explode::offset,
                "radius", LevelBasedValue.CODEC, Explode::radius,
                "create_fire", Codec.BOOLEAN, Explode::createFire,
                "block_interaction", BlockInteraction.CODEC, Explode::blockInteraction,
                "small_particle", Particle.CODEC, Explode::smallParticle,
                "large_particle", Particle.CODEC, Explode::largeParticle,
                "sound", SoundEvent.CODEC, Explode::sound,
                Explode::new);

        @Override
        public @NotNull StructCodec<Explode> codec() {
            return CODEC;
        }

        public enum BlockInteraction {
            NONE,
            BLOCK,
            MOB,
            TNT,
            TRIGGER;

            public static final Codec<BlockInteraction> CODEC = Codec.Enum(BlockInteraction.class);
        }
    }

    record Ignite(@NotNull LevelBasedValue duration) implements EntityEffect, LocationEffect {
        public static final StructCodec<Ignite> CODEC = StructCodec.struct(
                "duration", LevelBasedValue.CODEC, Ignite::duration,
                Ignite::new);

        @Override
        public @NotNull StructCodec<Ignite> codec() {
            return CODEC;
        }
    }

    record PlaySound(
            @NotNull SoundEvent sound,
            @NotNull FloatProvider volume,
            @NotNull FloatProvider pitch
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<PlaySound> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, PlaySound::sound,
                "volume", FloatProvider.CODEC, PlaySound::volume,
                "pitch", FloatProvider.CODEC, PlaySound::pitch,
                PlaySound::new);

        @Override
        public @NotNull StructCodec<PlaySound> codec() {
            return CODEC;
        }
    }

    record ReplaceBlock(
            @NotNull BlockStateProvider blockState,
            @Nullable Point offset,
            @Nullable BlockPredicate predicate,
            @Nullable Key triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<ReplaceBlock> CODEC = StructCodec.struct(
                "block_state", BlockStateProvider.CODEC, ReplaceBlock::blockState,
                "offset", Codec.BLOCK_POSITION, ReplaceBlock::offset,
                "predicate", BlockPredicate.CODEC, ReplaceBlock::predicate,
                "trigger_game_event", Codec.KEY, ReplaceBlock::triggerGameEvent,
                ReplaceBlock::new);

        @Override
        public @NotNull StructCodec<ReplaceBlock> codec() {
            return CODEC;
        }
    }

    record ReplaceDisc(
            @NotNull BlockStateProvider blockState,
            @NotNull LevelBasedValue radius,
            @NotNull LevelBasedValue height,
            @Nullable Point offset,
            @Nullable BlockPredicate predicate,
            @Nullable Key triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<ReplaceDisc> CODEC = StructCodec.struct(
                "block_state", BlockStateProvider.CODEC, ReplaceDisc::blockState,
                "radius", LevelBasedValue.CODEC, ReplaceDisc::radius,
                "height", LevelBasedValue.CODEC, ReplaceDisc::height,
                "offset", Codec.BLOCK_POSITION, ReplaceDisc::offset,
                "predicate", BlockPredicate.CODEC, ReplaceDisc::predicate,
                "trigger_game_event", Codec.KEY, ReplaceDisc::triggerGameEvent,
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
            @NotNull ItemBlockState properties,
            @Nullable Point offset,
            @Nullable Key triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<SetBlockProperties> CODEC = StructCodec.struct(
                "properties", ItemBlockState.CODEC, SetBlockProperties::properties,
                "offset", Codec.BLOCK_POSITION, SetBlockProperties::offset,
                "trigger_game_event", Codec.KEY, SetBlockProperties::triggerGameEvent,
                SetBlockProperties::new);

        @Override
        public @NotNull StructCodec<SetBlockProperties> codec() {
            return CODEC;
        }
    }

    record SpawnParticles(
            @NotNull Particle particle,
            @NotNull PositionSource horizontalPosition,
            @NotNull PositionSource verticalPosition,
            @NotNull VelocitySource horizontalVelocity,
            @NotNull VelocitySource verticalVelocity,
            @Nullable FloatProvider speed
    ) implements EntityEffect, LocationEffect {
        public static final StructCodec<SpawnParticles> CODEC = StructCodec.struct(
                "particle", Particle.CODEC, SpawnParticles::particle,
                "horizontal_position", PositionSource.CODEC, SpawnParticles::horizontalPosition,
                "vertical_position", PositionSource.CODEC, SpawnParticles::verticalPosition,
                "horizontal_velocity", VelocitySource.CODEC, SpawnParticles::horizontalVelocity,
                "vertical_velocity", VelocitySource.CODEC, SpawnParticles::verticalVelocity,
                "speed", FloatProvider.CODEC, SpawnParticles::speed,
                SpawnParticles::new);

        @Override
        public @NotNull StructCodec<SpawnParticles> codec() {
            return CODEC;
        }

        public record PositionSource(@NotNull PositionSource.Type type, float offset, float scale) {
            public static final StructCodec<PositionSource> CODEC = StructCodec.struct(
                    "type", Type.CODEC, PositionSource::type,
                    "offset", Codec.FLOAT.optional(0f), PositionSource::offset,
                    "scale", Codec.FLOAT.optional(1f), PositionSource::scale,
                    PositionSource::new);

            public enum Type {
                ENTITY_POSITION,
                IN_BOUNDING_BOX;

                public static final Codec<Type> CODEC = Codec.Enum(Type.class);
            }
        }

        public record VelocitySource(@Nullable FloatProvider base, float movementScale) {
            public static final StructCodec<VelocitySource> CODEC = StructCodec.struct(
                    "base", FloatProvider.CODEC.optional(), VelocitySource::base,
                    "movement_scale", Codec.FLOAT.optional(0f), VelocitySource::movementScale,
                    VelocitySource::new);
        }
    }

    record SummonEntity(
            @NotNull ObjectSet entity,
            boolean joinTeam
    ) implements EntityEffect, LocationEffect {
        private static final Codec<ObjectSet> ENTITY_CODEC = ObjectSet.codec(Tag.BasicType.ENTITY_TYPES);
        public static final StructCodec<SummonEntity> CODEC = StructCodec.struct(
                "entity", ObjectSet.codec(Tag.BasicType.ENTITY_TYPES), SummonEntity::entity,
                "join_team", Codec.BOOLEAN.optional(false), SummonEntity::joinTeam,
                SummonEntity::new);

        @Override
        public @NotNull StructCodec<SummonEntity> codec() {
            return CODEC;
        }
    }

}
