package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.Position;

public class EntityTeleportPacket implements ServerPacket {

    public int entityId;
    public Position position;
    public boolean onGround;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeByte((byte) (position.getYaw() * 256f / 360f));
        writer.writeByte((byte) (position.getPitch() * 256f / 360f));
        writer.writeBoolean(onGround);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_TELEPORT;
    }
}
