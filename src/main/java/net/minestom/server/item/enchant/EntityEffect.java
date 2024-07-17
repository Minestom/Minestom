package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;


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

    record Explode(
            boolean attributeToUser,
            @Nullable DynamicRegistry.Key<DamageType> damageType,
            @Nullable ObjectSet<Block> immuneBlocks,
            @Nullable LevelBasedValue knockbackMultiplier,
            @Nullable Point offset,
            @NotNull LevelBasedValue radius,
            boolean createFire,
            @NotNull BlockInteraction blockInteraction,
            @NotNull Particle smallParticle,
            @NotNull Particle largeParticle,
            @NotNull SoundEvent sound
    ) implements EntityEffect, LocationEffect {
        private static final BinaryTagSerializer<ObjectSet<Block>> IMMUNE_BLOCKS_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.BLOCKS);
        public static final BinaryTagSerializer<Explode> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Explode value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

                builder.putBoolean("attribute_to_user", value.attributeToUser);
                if (value.damageType != null)
                    builder.put("damage_type", DamageType.NBT_TYPE.write(context, value.damageType));
                if (value.immuneBlocks != null)
                    builder.put("immune_blocks", IMMUNE_BLOCKS_NBT_TYPE.write(context, value.immuneBlocks));
                if (value.knockbackMultiplier != null)
                    builder.put("knockback_multiplier", LevelBasedValue.NBT_TYPE.write(context, value.knockbackMultiplier));
                if (value.offset != null)
                    builder.put("offset", BinaryTagSerializer.BLOCK_POSITION.write(context, value.offset));
                builder.put("radius", LevelBasedValue.NBT_TYPE.write(context, value.radius));
                builder.putBoolean("create_fire", value.createFire);
                builder.putString("block_interaction", value.blockInteraction.id());
                builder.put("small_particle", Particle.NBT_TYPE.write(context, value.smallParticle));
                builder.put("large_particle", Particle.NBT_TYPE.write(context, value.largeParticle));
                builder.putString("sound", value.sound.namespace().asString());

                return builder.build();
            }

            @Override
            public @NotNull Explode read(@NotNull Context context, @NotNull BinaryTag tag) {
                final CompoundBinaryTag compound = (CompoundBinaryTag) tag;

                boolean attributeToUser = compound.getBoolean("attribute_to_user");
                BinaryTag damageTypeTag = compound.get("damage_type");
                DynamicRegistry.Key<DamageType> damageType = damageTypeTag == null ? null : DamageType.NBT_TYPE.read(damageTypeTag);
                BinaryTag immuneBlocksTag = compound.get("immune_blocks");
                ObjectSet<Block> immuneBlocks = immuneBlocksTag == null ? null : IMMUNE_BLOCKS_NBT_TYPE.read(immuneBlocksTag);
                BinaryTag knockbackMultiplierTag = compound.get("knockback_multiplier");
                LevelBasedValue knockbackMultiplier = knockbackMultiplierTag == null ? null : LevelBasedValue.NBT_TYPE.read(knockbackMultiplierTag);
                BinaryTag offsetTag = compound.get("offset");
                Point offset = offsetTag == null ? null : BinaryTagSerializer.BLOCK_POSITION.read(offsetTag);
                LevelBasedValue radius = LevelBasedValue.NBT_TYPE.read(Objects.requireNonNull(compound.get("radius")));
                boolean createFire = compound.getBoolean("create_fire");
                BlockInteraction blockInteraction = BlockInteraction.fromId(compound.getString("block_interaction"));
                Particle smallParticle = Particle.NBT_TYPE.read(Objects.requireNonNull(compound.get("small_particle")));
                Particle largeParticle = Particle.NBT_TYPE.read(Objects.requireNonNull(compound.get("large_particle")));
                SoundEvent sound = SoundEvent.fromNamespaceId(Objects.requireNonNull(compound.getString("sound")));
                Check.notNull(sound, "Cannot find sound event");

                return new Explode(
                        attributeToUser, damageType, immuneBlocks,
                        knockbackMultiplier, offset, radius, createFire,
                        blockInteraction, smallParticle, largeParticle, sound
                );
            }
        };

        @Override
        public @NotNull StructCodec<Explode> codec() {
            return CODEC;
        }

        public enum BlockInteraction {
            NONE("none"),
            BLOCK("block"),
            MOB("mob"),
            TNT("tnt"),
            TRIGGER("trigger");

            private final String id;

            BlockInteraction(String id) {
                this.id = id;
            }

            public String id() {
                return id;
            }

            public static @NotNull BlockInteraction fromId(String id) {
                for (BlockInteraction blockInteraction : values()) {
                    if (blockInteraction.id.equals(id)) {
                        return blockInteraction;
                    }
                }

                return NONE;
            }
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
