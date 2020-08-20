package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

public class PlayerPositionAndLookPacket implements ServerPacket {

    public Position position;
    public byte flags;
    public int teleportId;


    @Override
    public void write(BinaryWriter writer) {
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
        return ServerPacketIdentifier.PLAYER_POSITION_AND_LOOK;
    }
}