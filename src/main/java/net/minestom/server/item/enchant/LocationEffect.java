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

public non-sealed interface LocationEffect extends Enchantment.Effect {

    @NotNull Codec<LocationEffect> CODEC = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentLocationEffects, LocationEffect::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Codec<? extends LocationEffect>> createDefaultRegistry() {
        final DynamicRegistry<Codec<? extends LocationEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("all_of", AllOf.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("apply_mob_effect", EntityEffect.ApplyPotionEffect.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("attribute", AttributeEffect.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("change_item_damage", EntityEffect.ChangeItemDamage.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("damage_entity", EntityEffect.DamageEntity.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("explode", EntityEffect.Explode.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("ignite", EntityEffect.Ignite.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("play_sound", EntityEffect.PlaySound.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("replace_block", EntityEffect.ReplaceBlock.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("replace_disk", EntityEffect.ReplaceDisc.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("run_function", EntityEffect.RunFunction.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("set_block_properties", EntityEffect.SetBlockProperties.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("spawn_particles", EntityEffect.SpawnParticles.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("summon_entity", EntityEffect.SummonEntity.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    @NotNull Codec<? extends LocationEffect> codec();

    record AllOf(@NotNull List<LocationEffect> effect) implements LocationEffect {
        public static final Codec<AllOf> CODEC = StructCodec.struct(
                "effects", LocationEffect.CODEC.list(), AllOf::effect,
                AllOf::new
        );

        public AllOf {
            effect = List.copyOf(effect);
        }

        @Override
        public @NotNull Codec<AllOf> codec() {
            return CODEC;
        }
    }

}
