package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Equippable(
        @NotNull EquipmentSlot slot,
        @NotNull SoundEvent equipSound,
        @Nullable String assetId,
        @Nullable String cameraOverlay,
        @Nullable RegistryTag<EntityType> allowedEntities,
        boolean dispensable,
        boolean swappable,
        boolean damageOnHurt,
        boolean equipOnInteract,
        boolean canBeSheared,
        @NotNull SoundEvent shearingSound
) {
    public static final SoundEvent DEFAULT_EQUIP_SOUND = SoundEvent.ITEM_ARMOR_EQUIP_GENERIC;
    public static final SoundEvent DEFAULT_SHEARING_SOUND = SoundEvent.ITEM_SHEARS_SNIP;

    public static final NetworkBuffer.Type<Equippable> NETWORK_TYPE = NetworkBufferTemplate.template(
            EquipmentSlot.NETWORK_TYPE, Equippable::slot,
            SoundEvent.NETWORK_TYPE, Equippable::equipSound,
            NetworkBuffer.STRING.optional(), Equippable::assetId,
            NetworkBuffer.STRING.optional(), Equippable::cameraOverlay,
            RegistryTag.networkType(Registries::entityType).optional(), Equippable::allowedEntities,
            NetworkBuffer.BOOLEAN, Equippable::dispensable,
            NetworkBuffer.BOOLEAN, Equippable::swappable,
            NetworkBuffer.BOOLEAN, Equippable::damageOnHurt,
            NetworkBuffer.BOOLEAN, Equippable::equipOnInteract,
            NetworkBuffer.BOOLEAN, Equippable::canBeSheared,
            SoundEvent.NETWORK_TYPE, Equippable::shearingSound,
            Equippable::new);
    public static final Codec<Equippable> CODEC = StructCodec.struct(
            "slot", EquipmentSlot.CODEC, Equippable::slot,
            "equip_sound", SoundEvent.CODEC.optional(DEFAULT_EQUIP_SOUND), Equippable::equipSound,
            "asset_id", Codec.STRING.optional(), Equippable::assetId,
            "camera_overlay", Codec.STRING.optional(), Equippable::cameraOverlay,
            "allowed_entities", RegistryTag.codec(Registries::entityType).optional(), Equippable::allowedEntities,
            "dispensable", Codec.BOOLEAN.optional(true), Equippable::dispensable,
            "swappable", Codec.BOOLEAN.optional(true), Equippable::swappable,
            "damage_on_hurt", Codec.BOOLEAN.optional(true), Equippable::damageOnHurt,
            "equip_on_interact", Codec.BOOLEAN.optional(false), Equippable::equipOnInteract,
            "can_be_sheared", Codec.BOOLEAN.optional(false), Equippable::canBeSheared,
            "shearing_sound", SoundEvent.CODEC.optional(DEFAULT_SHEARING_SOUND), Equippable::shearingSound,
            Equippable::new);

    public @NotNull Equippable withSlot(@NotNull EquipmentSlot slot) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withEquipSound(@NotNull SoundEvent equipSound) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withAssetId(@Nullable String assetId) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withCameraOverlay(@Nullable String cameraOverlay) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withAllowedEntities(@Nullable RegistryTag<EntityType> allowedEntities) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withDispensable(boolean dispensable) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withSwappable(boolean swappable) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withDamageOnHurt(boolean damageOnHurt) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withEquipOnInteract(boolean equipOnInteract) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withCanBeSheared(boolean canBeSheared) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }

    public @NotNull Equippable withShearingSound(@NotNull SoundEvent shearingSound) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }
}
