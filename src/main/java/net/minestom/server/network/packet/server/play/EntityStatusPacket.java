package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityStatusPacket implements ServerPacket {

    public int entityId;
    public byte status;

    public EntityStatusPacket(int entityId, byte status) {
        this.entityId = entityId;
        this.status = status;
    }

    public EntityStatusPacket() {
        this(0, (byte) 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(entityId);
        writer.writeByte(status);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readInt();
        status = reader.readByte();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_STATUS;
    }
}
