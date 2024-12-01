package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.condition.BlockPredicate;
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
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public non-sealed interface EntityEffect extends Enchantment.Effect {

    @NotNull BinaryTagSerializer<EntityEffect> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentEntityEffects, EntityEffect::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("all_of", AllOf.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("apply_mob_effect", ApplyPotionEffect.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("damage_entity", DamageEntity.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("damage_item", DamageItem.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("explode", Explode.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("ignite", Ignite.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("play_sound", PlaySound.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("replace_block", ReplaceBlock.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("replace_disk", ReplaceDisc.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("run_function", RunFunction.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("set_block_properties", SetBlockProperties.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("spawn_particles", SpawnParticles.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("summon_entity", SummonEntity.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    @NotNull BinaryTagSerializer<? extends EntityEffect> nbtType();

    record AllOf(@NotNull List<EntityEffect> effect) implements EntityEffect {
        public static final BinaryTagSerializer<AllOf> NBT_TYPE = BinaryTagSerializer.object(
                "effects", EntityEffect.NBT_TYPE.list(), AllOf::effect,
                AllOf::new
        );

        public AllOf {
            effect = List.copyOf(effect);
        }

        @Override
        public @NotNull BinaryTagSerializer<AllOf> nbtType() {
            return NBT_TYPE;
        }
    }

    record ApplyPotionEffect(
            @NotNull ObjectSet toApply,
            @NotNull LevelBasedValue minDuration,
            @NotNull LevelBasedValue maxDuration,
            @NotNull LevelBasedValue minAmplifier,
            @NotNull LevelBasedValue maxAmplifier
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ApplyPotionEffect> NBT_TYPE = BinaryTagSerializer.object(
                "to_apply", ObjectSet.nbtType(Tag.BasicType.POTION_EFFECTS), ApplyPotionEffect::toApply,
                "min_duration", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::minDuration,
                "max_duration", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::maxDuration,
                "min_amplifier", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::minAmplifier,
                "max_amplifier", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::maxAmplifier,
                ApplyPotionEffect::new
        );

        @Override
        public @NotNull BinaryTagSerializer<ApplyPotionEffect> nbtType() {
            return NBT_TYPE;
        }
    }

    record DamageEntity(
            @NotNull DynamicRegistry.Key<DamageType> damageType,
            @NotNull LevelBasedValue minDamage,
            @NotNull LevelBasedValue maxDamage
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<DamageEntity> NBT_TYPE = BinaryTagSerializer.object(
                "damage_type", DamageType.NBT_TYPE, DamageEntity::damageType,
                "min_damage", LevelBasedValue.NBT_TYPE, DamageEntity::minDamage,
                "max_damage", LevelBasedValue.NBT_TYPE, DamageEntity::maxDamage,
                DamageEntity::new
        );

        @Override
        public @NotNull BinaryTagSerializer<DamageEntity> nbtType() {
            return NBT_TYPE;
        }
    }

    record DamageItem(@NotNull LevelBasedValue amount) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<DamageItem> NBT_TYPE = BinaryTagSerializer.object(
                "amount", LevelBasedValue.NBT_TYPE, DamageItem::amount,
                DamageItem::new
        );

        @Override
        public @NotNull BinaryTagSerializer<DamageItem> nbtType() {
            return NBT_TYPE;
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
        private static final BinaryTagSerializer<ObjectSet> IMMUNE_BLOCKS_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.BLOCKS);
        public static final BinaryTagSerializer<Explode> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Explode value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

                builder.putBoolean("attribute_to_user", value.attributeToUser());
                if (value.damageType() != null)
                    builder.put("damage_type", DamageType.NBT_TYPE.write(context, value.damageType()));
                if (value.immuneBlocks() != null)
                    builder.put("immune_blocks", IMMUNE_BLOCKS_NBT_TYPE.write(context, value.immuneBlocks()));
                if (value.knockbackMultiplier() != null)
                    builder.put("knockback_multiplier", LevelBasedValue.NBT_TYPE.write(context, value.knockbackMultiplier()));
                if (value.offset() != null)
                    builder.put("offset", BinaryTagSerializer.BLOCK_POSITION.write(context, value.offset()));
                builder.put("radius", LevelBasedValue.NBT_TYPE.write(context, value.radius()));
                builder.putBoolean("create_fire", value.createFire);
                builder.put("block_interaction", BlockInteraction.NBT_TYPE.write(value.blockInteraction()));
                builder.put("small_particle", Particle.NBT_TYPE.write(context, value.smallParticle()));
                builder.put("large_particle", Particle.NBT_TYPE.write(context, value.largeParticle()));
                builder.put("sound", SoundEvent.NBT_TYPE.write(value.sound()));

                return builder.build();
            }

            @Override
            public @NotNull Explode read(@NotNull Context context, @NotNull BinaryTag tag) {
                final CompoundBinaryTag compound = (CompoundBinaryTag) tag;

                boolean attributeToUser = compound.getBoolean("attribute_to_user", false);
                BinaryTag damageTypeTag = compound.get("damage_type");
                DynamicRegistry.Key<DamageType> damageType = damageTypeTag == null ? null : DamageType.NBT_TYPE.read(context, damageTypeTag);
                BinaryTag immuneBlocksTag = compound.get("immune_blocks");
                ObjectSet immuneBlocks = immuneBlocksTag == null ? null : IMMUNE_BLOCKS_NBT_TYPE.read(context, immuneBlocksTag);
                BinaryTag knockbackMultiplierTag = compound.get("knockback_multiplier");
                LevelBasedValue knockbackMultiplier = knockbackMultiplierTag == null ? null : LevelBasedValue.NBT_TYPE.read(context, knockbackMultiplierTag);
                BinaryTag offsetTag = compound.get("offset");
                Point offset = offsetTag == null ? null : BinaryTagSerializer.BLOCK_POSITION.read(context, offsetTag);
                LevelBasedValue radius = LevelBasedValue.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("radius")));
                boolean createFire = compound.getBoolean("create_fire", false);
                BlockInteraction blockInteraction = BlockInteraction.NBT_TYPE.read(Objects.requireNonNull(compound.get("block_interaction")));
                Particle smallParticle = Particle.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("small_particle")));
                Particle largeParticle = Particle.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("large_particle")));
                SoundEvent sound = SoundEvent.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("sound")));
                Check.notNull(sound, "Cannot find sound event");

                return new Explode(
                        attributeToUser, damageType, immuneBlocks,
                        knockbackMultiplier, offset, radius, createFire,
                        blockInteraction, smallParticle, largeParticle, sound
                );
            }
        };

        @Override
        public @NotNull BinaryTagSerializer<Explode> nbtType() {
            return NBT_TYPE;
        }

        public enum BlockInteraction {
            NONE,
            BLOCK,
            MOB,
            TNT,
            TRIGGER;

            public static final BinaryTagSerializer<BlockInteraction> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(BlockInteraction.class);
        }
    }

    record Ignite(@NotNull LevelBasedValue duration) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<Ignite> NBT_TYPE = BinaryTagSerializer.object(
                "duration", LevelBasedValue.NBT_TYPE, Ignite::duration,
                Ignite::new
        );

        @Override
        public @NotNull BinaryTagSerializer<Ignite> nbtType() {
            return NBT_TYPE;
        }
    }

    record PlaySound(
            @NotNull SoundEvent sound,
            @NotNull FloatProvider volume,
            @NotNull FloatProvider pitch
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<PlaySound> NBT_TYPE = BinaryTagSerializer.object(
                "sound", SoundEvent.NBT_TYPE, PlaySound::sound,
                "volume", FloatProvider.NBT_TYPE, PlaySound::volume,
                "pitch", FloatProvider.NBT_TYPE, PlaySound::pitch,
                PlaySound::new
        );

        @Override
        public @NotNull BinaryTagSerializer<PlaySound> nbtType() {
            return NBT_TYPE;
        }
    }

    record ReplaceBlock(
            @NotNull BlockStateProvider blockState,
            @Nullable Point offset,
            @Nullable BlockPredicate predicate,
            @Nullable NamespaceID triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ReplaceBlock> NBT_TYPE = BinaryTagSerializer.object(
                "block_state", BlockStateProvider.NBT_TYPE, ReplaceBlock::blockState,
                "offset", BinaryTagSerializer.BLOCK_POSITION, ReplaceBlock::offset,
                "predicate", BlockPredicate.NBT_TYPE, ReplaceBlock::predicate,
                "trigger_game_event", BinaryTagSerializer.STRING.map(NamespaceID::from, NamespaceID::asString), ReplaceBlock::triggerGameEvent,
                ReplaceBlock::new
        );

        @Override
        public @NotNull BinaryTagSerializer<ReplaceBlock> nbtType() {
            return NBT_TYPE;
        }
    }

    record ReplaceDisc(
            @NotNull BlockStateProvider blockState,
            @NotNull LevelBasedValue radius,
            @NotNull LevelBasedValue height,
            @Nullable Point offset,
            @Nullable BlockPredicate predicate,
            @Nullable NamespaceID triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ReplaceDisc> NBT_TYPE = BinaryTagSerializer.object(
                "block_state", BlockStateProvider.NBT_TYPE, ReplaceDisc::blockState,
                "radius", LevelBasedValue.NBT_TYPE, ReplaceDisc::radius,
                "height", LevelBasedValue.NBT_TYPE, ReplaceDisc::height,
                "offset", BinaryTagSerializer.BLOCK_POSITION, ReplaceDisc::offset,
                "predicate", BlockPredicate.NBT_TYPE, ReplaceDisc::predicate,
                "trigger_game_event", BinaryTagSerializer.STRING.map(NamespaceID::from, NamespaceID::asString), ReplaceDisc::triggerGameEvent,
                ReplaceDisc::new
        );

        @Override
        public @NotNull BinaryTagSerializer<ReplaceDisc> nbtType() {
            return NBT_TYPE;
        }
    }

    record RunFunction(
            @NotNull String function
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<RunFunction> NBT_TYPE = BinaryTagSerializer.object(
                "function", BinaryTagSerializer.STRING, RunFunction::function,
                RunFunction::new
        );

        @Override
        public @NotNull BinaryTagSerializer<RunFunction> nbtType() {
            return NBT_TYPE;
        }
    }

    record SetBlockProperties(
            @NotNull ItemBlockState properties,
            @Nullable Point offset,
            @Nullable NamespaceID triggerGameEvent
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SetBlockProperties> NBT_TYPE = BinaryTagSerializer.object(
                "properties", ItemBlockState.NBT_TYPE, SetBlockProperties::properties,
                "offset", BinaryTagSerializer.BLOCK_POSITION, SetBlockProperties::offset,
                "trigger_game_event", BinaryTagSerializer.STRING.map(NamespaceID::from, NamespaceID::asString), SetBlockProperties::triggerGameEvent,
                SetBlockProperties::new
        );

        @Override
        public @NotNull BinaryTagSerializer<SetBlockProperties> nbtType() {
            return NBT_TYPE;
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
        public static final BinaryTagSerializer<SpawnParticles> NBT_TYPE = BinaryTagSerializer.object(
                "particle", Particle.NBT_TYPE, SpawnParticles::particle,
                "horizontal_position", PositionSource.NBT_TYPE, SpawnParticles::horizontalPosition,
                "vertical_position", PositionSource.NBT_TYPE, SpawnParticles::verticalPosition,
                "horizontal_velocity", VelocitySource.NBT_TYPE, SpawnParticles::horizontalVelocity,
                "vertical_velocity", VelocitySource.NBT_TYPE, SpawnParticles::verticalVelocity,
                "speed", FloatProvider.NBT_TYPE, SpawnParticles::speed,
                SpawnParticles::new
        );

        @Override
        public @NotNull BinaryTagSerializer<SpawnParticles> nbtType() {
            return NBT_TYPE;
        }

        public record PositionSource(@NotNull PositionSource.Type type, float offset, float scale) {
            public static final BinaryTagSerializer<PositionSource> NBT_TYPE = new BinaryTagSerializer<>() {
                @Override
                public @NotNull BinaryTag write(@NotNull Context context, @NotNull PositionSource value) {
                    CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                    builder.put("type", Type.NBT_TYPE.write(context, value.type()));
                    if (value.offset() != 0) builder.putFloat("offset", value.offset());
                    if (value.scale() != 1) builder.putFloat("scale", value.scale());
                    return builder.build();
                }

                @Override
                public @NotNull PositionSource read(@NotNull Context context, @NotNull BinaryTag tag) {
                    if (!(tag instanceof CompoundBinaryTag compound))
                        throw new IllegalArgumentException("Compound expected for position source");
                    Type type = Type.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("type")));
                    BinaryTag offsetTag = compound.get("offset");
                    float offset = offsetTag instanceof NumberBinaryTag number ? number.floatValue() : 0f;
                    BinaryTag scaleTag = compound.get("scale");
                    float scale = scaleTag instanceof NumberBinaryTag number ? number.floatValue() : 1f;

                    return new PositionSource(type, offset, scale);
                }
            };

            public enum Type {
                ENTITY_POSITION,
                IN_BOUNDING_BOX;

                public static final BinaryTagSerializer<Type> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Type.class);
            }
        }

        public record VelocitySource(@Nullable FloatProvider base, float movementScale) {
            public static final BinaryTagSerializer<VelocitySource> NBT_TYPE = new BinaryTagSerializer<>() {
                @Override
                public @NotNull BinaryTag write(@NotNull Context context, @NotNull VelocitySource value) {
                    CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                    if (value.base() != null) builder.put("base", FloatProvider.NBT_TYPE.write(context, value.base()));
                    if (value.movementScale() != 0) builder.putFloat("movement_scale", value.movementScale());
                    return builder.build();
                }

                @Override
                public @NotNull VelocitySource read(@NotNull Context context, @NotNull BinaryTag tag) {
                    if (!(tag instanceof CompoundBinaryTag compound))
                        throw new IllegalArgumentException("Compound expected for velocity source");
                    BinaryTag baseTag = compound.get("base");
                    FloatProvider base = baseTag instanceof CompoundBinaryTag baseCompound ? FloatProvider.NBT_TYPE.read(context, baseCompound) : null;
                    BinaryTag movementScaleTag = compound.get("movement_scale");
                    float movementScale = movementScaleTag instanceof NumberBinaryTag number ? number.floatValue() : 0f;

                    return new VelocitySource(base, movementScale);
                }
            };
        }
    }

    record SummonEntity(
            @NotNull ObjectSet entity,
            boolean joinTeam
    ) implements EntityEffect, LocationEffect {
        private static final BinaryTagSerializer<ObjectSet> ENTITY_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.ENTITY_TYPES);
        public static final BinaryTagSerializer<SummonEntity> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull SummonEntity value) {
                return CompoundBinaryTag.builder()
                        .put("entity", ENTITY_NBT_TYPE.write(value.entity()))
                        .putBoolean("join_team", value.joinTeam())
                        .build();
            }

            @Override
            public @NotNull SummonEntity read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound))
                    throw new IllegalArgumentException("Compound expected for summon entity");
                return new SummonEntity(
                        ENTITY_NBT_TYPE.read(Objects.requireNonNull(compound.get("entity"))),
                        compound.getBoolean("join_team", false)
                );
            }
        };

        @Override
        public @NotNull BinaryTagSerializer<SummonEntity> nbtType() {
            return NBT_TYPE;
        }
    }

}
