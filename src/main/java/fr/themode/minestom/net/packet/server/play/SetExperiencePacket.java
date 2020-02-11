package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
