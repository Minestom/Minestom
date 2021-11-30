package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EntityStatusPacket(int entityId, byte status) implements ServerPacket {
    public EntityStatusPacket(BinaryReader reader) {
        this(reader.readInt(), reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(entityId);
        writer.writeByte(status);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_STATUS;
    }
}
