package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class SetExperiencePacket implements ServerPacket {

    public float percentage;
    public int level;
    public int totalExperience;

    @Override
    public void write(PacketWriter writer) {
        writer.writeFloat(percentage);
        writer.writeVarInt(level);
        writer.writeVarInt(totalExperience);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_EXPERIENCE;
    }
}
