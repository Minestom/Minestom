package net.minestom.server.item.component;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Equippable(
        @NotNull EquipmentSlot slot,
        @NotNull SoundEvent equipSound,
        @Nullable String model,
        @Nullable String cameraOverlay,
//        @Nullable List<EntityType> allowedEntities, // TODO(1.21.2): this is an ObjectSet
        boolean dispensable,
        boolean swappable,
        boolean damageOnHurt
        ) {

    public static final NetworkBuffer.Type<Equippable> NETWORK_TYPE = NetworkBufferTemplate.template(
            EquipmentSlot.NETWORK_TYPE, Equippable::slot,
            SoundEvent.NETWORK_TYPE, Equippable::equipSound,
            NetworkBuffer.STRING.optional(), Equippable::model,
            NetworkBuffer.STRING.optional(), Equippable::cameraOverlay,
//            STRING.list().optional(), Equippable::allowedEntities,
            NetworkBuffer.BOOLEAN, Equippable::dispensable,
            NetworkBuffer.BOOLEAN, Equippable::swappable,
            NetworkBuffer.BOOLEAN, Equippable::damageOnHurt,
            Equippable::new);
    public static final BinaryTagSerializer<Equippable> NBT_TYPE = BinaryTagSerializer.object(
            "slot", EquipmentSlot.NBT_TYPE, Equippable::slot,
            "equip_sound", SoundEvent.NBT_TYPE, Equippable::equipSound,
            "model", BinaryTagSerializer.STRING.optional(), Equippable::model,
            "camera_overlay", BinaryTagSerializer.STRING.optional(), Equippable::cameraOverlay,
//            "allowed_entities", STRING.list().optional(), Equippable::allowedEntities,
            "dispensable", BinaryTagSerializer.BOOLEAN, Equippable::dispensable,
            "swappable", BinaryTagSerializer.BOOLEAN, Equippable::swappable,
            "damage_on_hurt", BinaryTagSerializer.BOOLEAN, Equippable::damageOnHurt,
            Equippable::new);
}
