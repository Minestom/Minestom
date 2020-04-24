package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class UpdateHealthPacket implements ServerPacket {

    public float health;
    public int food;
    public float foodSaturation;

    @Override
    public void write(PacketWriter writer) {
        writer.writeFloat(health);
        writer.writeVarInt(food);
        writer.writeFloat(foodSaturation);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_HEALTH;
    }
}
