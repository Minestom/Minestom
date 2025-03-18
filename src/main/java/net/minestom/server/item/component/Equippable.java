package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Equippable(
        @NotNull EquipmentSlot slot,
        @NotNull SoundEvent equipSound,
        @Nullable String assetId,
        @Nullable String cameraOverlay,
        @Nullable ObjectSet<EntityType> allowedEntities,
        boolean dispensable,
        boolean swappable,
        boolean damageOnHurt
) {
    public static final NetworkBuffer.Type<Equippable> NETWORK_TYPE = NetworkBufferTemplate.template(
            EquipmentSlot.NETWORK_TYPE, Equippable::slot,
            SoundEvent.NETWORK_TYPE, Equippable::equipSound,
            NetworkBuffer.STRING.optional(), Equippable::assetId,
            NetworkBuffer.STRING.optional(), Equippable::cameraOverlay,
            ObjectSet.<EntityType>networkType(Tag.BasicType.ENTITY_TYPES).optional(), Equippable::allowedEntities,
            NetworkBuffer.BOOLEAN, Equippable::dispensable,
            NetworkBuffer.BOOLEAN, Equippable::swappable,
            NetworkBuffer.BOOLEAN, Equippable::damageOnHurt,
            Equippable::new);
    public static final Codec<Equippable> CODEC = StructCodec.struct(
            "slot", EquipmentSlot.CODEC, Equippable::slot,
            "equip_sound", SoundEvent.CODEC.optional(SoundEvent.ITEM_ARMOR_EQUIP_GENERIC), Equippable::equipSound,
            "asset_id", Codec.STRING.optional(), Equippable::assetId,
            "camera_overlay", Codec.STRING.optional(), Equippable::cameraOverlay,
            "allowed_entities", ObjectSet.<EntityType>codec(Tag.BasicType.ENTITY_TYPES).optional(), Equippable::allowedEntities,
            "dispensable", Codec.BOOLEAN.optional(true), Equippable::dispensable,
            "swappable", Codec.BOOLEAN.optional(true), Equippable::swappable,
            "damage_on_hurt", Codec.BOOLEAN.optional(true), Equippable::damageOnHurt,
            Equippable::new);

    public @NotNull Equippable withSlot(@NotNull EquipmentSlot slot) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withEquipSound(@NotNull SoundEvent equipSound) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withAssetId(@Nullable String assetId) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withCameraOverlay(@Nullable String cameraOverlay) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withAllowedEntities(@Nullable ObjectSet<EntityType> allowedEntities) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withDispensable(boolean dispensable) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withSwappable(boolean swappable) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public @NotNull Equippable withDamageOnHurt(boolean damageOnHurt) {
        return new Equippable(slot, equipSound, assetId, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }
}
