package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DestroyEntityPacket implements ServerPacket {

    public int entityId;

    public DestroyEntityPacket() {
    }

    public static DestroyEntityPacket of(int entityId) {
        DestroyEntityPacket packet = new DestroyEntityPacket();
        packet.entityId = entityId;
        return packet;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.entityId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITY;
    }
}
