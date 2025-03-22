package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.CompoundBinaryTag;
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

    @NotNull Codec<EntityEffect> CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentEntityEffects, EntityEffect::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Codec<? extends EntityEffect>> createDefaultRegistry() {
        final DynamicRegistry<Codec<? extends EntityEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
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

    @NotNull Codec<? extends EntityEffect> codec();

    record AllOf(@NotNull List<EntityEffect> effect) implements EntityEffect {
        public static final Codec<AllOf> CODEC = StructCodec.struct(
                "effects", EntityEffect.CODEC.list(), AllOf::effect,
                AllOf::new);

        public AllOf {
            effect = List.copyOf(effect);
        }

        @Override
        public @NotNull Codec<AllOf> codec() {
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
        public static final Codec<ApplyPotionEffect> CODEC = StructCodec.struct(
                "to_apply", ObjectSet.codec(Tag.BasicType.POTION_EFFECTS), ApplyPotionEffect::toApply,
                "min_duration", LevelBasedValue.CODEC, ApplyPotionEffect::minDuration,
                "max_duration", LevelBasedValue.CODEC, ApplyPotionEffect::maxDuration,
                "min_amplifier", LevelBasedValue.CODEC, ApplyPotionEffect::minAmplifier,
                "max_amplifier", LevelBasedValue.CODEC, ApplyPotionEffect::maxAmplifier,
                ApplyPotionEffect::new
        );

        @Override
        public @NotNull Codec<ApplyPotionEffect> codec() {
            return CODEC;
        }
    }

    record DamageEntity(
            @NotNull DynamicRegistry.Key<DamageType> damageType,
            @NotNull LevelBasedValue minDamage,
            @NotNull LevelBasedValue maxDamage
    ) implements EntityEffect, LocationEffect {
        public static final Codec<DamageEntity> CODEC = StructCodec.struct(
                "damage_type", DamageType.CODEC, DamageEntity::damageType,
                "min_damage", LevelBasedValue.CODEC, DamageEntity::minDamage,
                "max_damage", LevelBasedValue.CODEC, DamageEntity::maxDamage,
                DamageEntity::new);

        @Override
        public @NotNull Codec<DamageEntity> codec() {
            return CODEC;
        }
    }

    record ChangeItemDamage(@NotNull LevelBasedValue amount) implements EntityEffect, LocationEffect {
        public static final Codec<ChangeItemDamage> CODEC = StructCodec.struct(
                "amount", LevelBasedValue.CODEC, ChangeItemDamage::amount,
                ChangeItemDamage::new
        );

        @Override
        public @NotNull Codec<ChangeItemDamage> codec() {
            return CODEC;
        }
    }

    record Explode(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final Codec<Explode> CODEC = Codec.NBT_COMPOUND.transform(Explode::new, Explode::content);

        @Override
        public @NotNull Codec<Explode> codec() {
            return CODEC;
        }
    }

    record Ignite(@NotNull LevelBasedValue duration) implements EntityEffect, LocationEffect {
        public static final Codec<Ignite> CODEC = StructCodec.struct(
                "duration", LevelBasedValue.CODEC, Ignite::duration,
                Ignite::new
        );

        @Override
        public @NotNull Codec<Ignite> codec() {
            return CODEC;
        }
    }

    record PlaySound(
            CompoundBinaryTag content
//            @NotNull SoundEvent sound,
//            Object volume, // "A Float Provider between 0.00001 and 10.0 specifying the volume of the sound"
//            Object pitch // "A Float Provider between 0.00001 and 2.0 specifying the pitch of the sound"
    ) implements EntityEffect, LocationEffect {
        public static final Codec<PlaySound> CODEC = Codec.NBT_COMPOUND.transform(PlaySound::new, PlaySound::content);

        @Override
        public @NotNull Codec<PlaySound> codec() {
            return CODEC;
        }
    }

    record ReplaceBlock(
            CompoundBinaryTag content
//            Object blockState, // "A block state provider giving the block state to set"
//            @NotNull Point offset,
//            @Nullable Object predicate // "A World-generation style Block Predicate to used to determine if the block should be replaced"
    ) implements EntityEffect, LocationEffect {
        public static final Codec<ReplaceBlock> CODEC = Codec.NBT_COMPOUND.transform(ReplaceBlock::new, ReplaceBlock::content);

        @Override
        public @NotNull Codec<ReplaceBlock> codec() {
            return CODEC;
        }
    }

    record ReplaceDisc(
            CompoundBinaryTag content
            // todo
    ) implements EntityEffect, LocationEffect {
        public static final Codec<ReplaceDisc> CODEC = Codec.NBT_COMPOUND.transform(ReplaceDisc::new, ReplaceDisc::content);

        @Override
        public @NotNull Codec<ReplaceDisc> codec() {
            return CODEC;
        }
    }

    record RunFunction(
            @NotNull String function
    ) implements EntityEffect, LocationEffect {
        public static final Codec<RunFunction> CODEC = StructCodec.struct(
                "function", Codec.STRING, RunFunction::function,
                RunFunction::new);

        @Override
        public @NotNull Codec<RunFunction> codec() {
            return CODEC;
        }
    }

    record SetBlockProperties(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final Codec<SetBlockProperties> CODEC = Codec.NBT_COMPOUND.transform(SetBlockProperties::new, SetBlockProperties::content);

        @Override
        public @NotNull Codec<SetBlockProperties> codec() {
            return CODEC;
        }
    }

    record SpawnParticles(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final Codec<SpawnParticles> CODEC = Codec.NBT_COMPOUND.transform(SpawnParticles::new, SpawnParticles::content);

        @Override
        public @NotNull Codec<SpawnParticles> codec() {
            return CODEC;
        }
    }

    record SummonEntity(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final Codec<SummonEntity> CODEC = Codec.NBT_COMPOUND.transform(SummonEntity::new, SummonEntity::content);

        @Override
        public @NotNull Codec<SummonEntity> codec() {
            return CODEC;
        }
    }

}
