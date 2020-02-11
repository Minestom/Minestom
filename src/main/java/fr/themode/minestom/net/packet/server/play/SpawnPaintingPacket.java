package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.BlockPosition;

import java.util.UUID;

public class SpawnPaintingPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int motive;
    public BlockPosition position;
    public byte direction;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(entityUuid);
        writer.writeVarInt(motive);
        writer.writeBlockPosition(position);
        writer.writeByte(direction);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_PAINTING;
    }
}
