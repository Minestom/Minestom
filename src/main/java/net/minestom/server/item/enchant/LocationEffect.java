package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public non-sealed interface LocationEffect extends Enchantment.Effect {

    StructCodec<LocationEffect> CODEC = Codec.RegistryTaggedUnion(
            Registries::enchantmentLocationEffects, LocationEffect::codec, "type");

    @ApiStatus.Internal
    static DynamicRegistry<StructCodec<? extends LocationEffect>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LocationEffect>> registry = DynamicRegistry.create(Key.key("minestom:enchantment_value_effect"));
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

    StructCodec<? extends LocationEffect> codec();

    record AllOf(List<LocationEffect> effect) implements LocationEffect {
        public static final StructCodec<AllOf> CODEC = StructCodec.struct(
                "effects", LocationEffect.CODEC.list(), AllOf::effect,
                AllOf::new
        );

        public AllOf {
            effect = List.copyOf(effect);
        }

        @Override
        public StructCodec<AllOf> codec() {
            return CODEC;
        }
    }

}
