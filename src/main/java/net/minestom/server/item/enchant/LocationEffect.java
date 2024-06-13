package net.minestom.server.item.enchant;

import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed interface LocationEffect extends Enchantment.Effect {

    @NotNull BinaryTagSerializer<LocationEffect> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentLocationEffects, LocationEffect::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends LocationEffect>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends LocationEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("all_of", AllOf.NBT_TYPE);
        registry.register("attribute", AttributeEffect.NBT_TYPE);
        registry.register("apply_mob_effect", EntityEffect.ApplyPotionEffect.NBT_TYPE);
        registry.register("damage_entity", EntityEffect.DamageEntity.NBT_TYPE);
        registry.register("damage_item", EntityEffect.DamageItem.NBT_TYPE);
        registry.register("explode", EntityEffect.Explode.NBT_TYPE);
        registry.register("ignite", EntityEffect.Ignite.NBT_TYPE);
        registry.register("play_sound", EntityEffect.PlaySound.NBT_TYPE);
        registry.register("replace_block", EntityEffect.ReplaceBlock.NBT_TYPE);
        registry.register("replace_disk", EntityEffect.ReplaceDisc.NBT_TYPE);
        registry.register("run_function", EntityEffect.RunFunction.NBT_TYPE);
        registry.register("set_block_properties", EntityEffect.SetBlockProperties.NBT_TYPE);
        registry.register("spawn_particles", EntityEffect.SpawnParticles.NBT_TYPE);
        registry.register("summon_entity", EntityEffect.SummonEntity.NBT_TYPE);
        return registry;
    }

    @NotNull BinaryTagSerializer<? extends LocationEffect> nbtType();

    record AllOf(@NotNull List<LocationEffect> effect) implements LocationEffect {
        public static final BinaryTagSerializer<AllOf> NBT_TYPE = BinaryTagSerializer.object(
                "effects", LocationEffect.NBT_TYPE.list(), AllOf::effect,
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

}
