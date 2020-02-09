package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;

public class PlayerPositionAndLookPacket implements ServerPacket {

    public Position position;
    public byte flags;
    public int teleportId;


    @Override
    public void write(PacketWriter writer) {
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeFloat(position.getYaw());
        writer.writeFloat(position.getPitch());
        writer.writeByte(flags);
        writer.writeVarInt(teleportId);
    }

    @Override
    public int getId() {
        return 0x36;
    }
}