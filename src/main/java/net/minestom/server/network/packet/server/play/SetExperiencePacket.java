package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SetExperiencePacket implements ServerPacket {

    public float percentage;
    public int level;
    public int totalExperience;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(percentage);
        writer.writeVarInt(level);
        writer.writeVarInt(totalExperience);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_EXPERIENCE;
    }
}
