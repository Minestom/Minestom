package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public non-sealed interface EntityEffect extends Enchantment.Effect {

    @NotNull BinaryTagSerializer<EntityEffect> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentEntityEffects, EntityEffect::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("all_of", AllOf.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("apply_mob_effect", ApplyPotionEffect.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("change_item_damage", ChangeItemDamage.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("damage_entity", DamageEntity.NBT_TYPE, DataPack.MINECRAFT_CORE);
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
            @NotNull ObjectSet<PotionEffect> toApply,
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

    record ChangeItemDamage(@NotNull LevelBasedValue amount) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ChangeItemDamage> NBT_TYPE = BinaryTagSerializer.object(
                "amount", LevelBasedValue.NBT_TYPE, ChangeItemDamage::amount,
                ChangeItemDamage::new
        );

        @Override
        public @NotNull BinaryTagSerializer<ChangeItemDamage> nbtType() {
            return NBT_TYPE;
        }
    }

    record Explode(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<Explode> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(Explode::new, Explode::content);

        @Override
        public @NotNull BinaryTagSerializer<Explode> nbtType() {
            return NBT_TYPE;
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
            CompoundBinaryTag content
//            @NotNull SoundEvent sound,
//            Object volume, // "A Float Provider between 0.00001 and 10.0 specifying the volume of the sound"
//            Object pitch // "A Float Provider between 0.00001 and 2.0 specifying the pitch of the sound"
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<PlaySound> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(PlaySound::new, PlaySound::content);

        @Override
        public @NotNull BinaryTagSerializer<PlaySound> nbtType() {
            return NBT_TYPE;
        }
    }

    record ReplaceBlock(
            CompoundBinaryTag content
//            Object blockState, // "A block state provider giving the block state to set"
//            @NotNull Point offset,
//            @Nullable Object predicate // "A World-generation style Block Predicate to used to determine if the block should be replaced"
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ReplaceBlock> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(ReplaceBlock::new, ReplaceBlock::content);

        @Override
        public @NotNull BinaryTagSerializer<ReplaceBlock> nbtType() {
            return NBT_TYPE;
        }
    }

    record ReplaceDisc(
            CompoundBinaryTag content
            // todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ReplaceDisc> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(ReplaceDisc::new, ReplaceDisc::content);

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
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SetBlockProperties> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(SetBlockProperties::new, SetBlockProperties::content);

        @Override
        public @NotNull BinaryTagSerializer<SetBlockProperties> nbtType() {
            return NBT_TYPE;
        }
    }

    record SpawnParticles(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SpawnParticles> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(SpawnParticles::new, SpawnParticles::content);

        @Override
        public @NotNull BinaryTagSerializer<SpawnParticles> nbtType() {
            return NBT_TYPE;
        }
    }

    record SummonEntity(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SummonEntity> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(SummonEntity::new, SummonEntity::content);

        @Override
        public @NotNull BinaryTagSerializer<SummonEntity> nbtType() {
            return NBT_TYPE;
        }
    }

}
