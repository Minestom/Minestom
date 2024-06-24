package net.minestom.scratch.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EntityInitPackets {

    public static List<ServerPacket.Play> playerInit(int id, UUID uuid, Pos position,
                                                     Map<EquipmentSlot, ItemStack> equipments, EntityMetaDataPacket metaDataPacket) {
        final var spawnPacket = new SpawnEntityPacket(
                id, uuid, EntityType.PLAYER.id(),
                position, 0, 0, (short) 0, (short) 0, (short) 0
        );

        List<ServerPacket.Play> packets = new ArrayList<>();
        packets.add(spawnPacket);
        if (!equipments.isEmpty()) packets.add(new EntityEquipmentPacket(id, equipments));
        if (metaDataPacket != null) packets.add(metaDataPacket);
        return List.copyOf(packets);
    }

    public static List<ServerPacket.Play> playerDestroy(int id) {
        return List.of(new DestroyEntitiesPacket(id));
    }

    public static List<ServerPacket.Play> entityInit(int id, UUID uuid, EntityType entityType, Pos position,
                                                     Map<EquipmentSlot, ItemStack> equipments, EntityMetaDataPacket metaDataPacket) {
        final var spawnPacket = new SpawnEntityPacket(
                id, uuid, entityType.id(),
                position, 0, 0, (short) 0, (short) 0, (short) 0
        );

        List<ServerPacket.Play> packets = new ArrayList<>();
        packets.add(spawnPacket);
        if (!equipments.isEmpty()) packets.add(new EntityEquipmentPacket(id, equipments));
        if (metaDataPacket != null) packets.add(metaDataPacket);
        return List.copyOf(packets);
    }

    public static List<ServerPacket.Play> entityDestroy(int id) {
        return List.of(new DestroyEntitiesPacket(id));
    }
}
