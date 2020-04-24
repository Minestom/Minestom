package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.world.Difficulty;

public class ServerDifficultyPacket implements ServerPacket {

    public Difficulty difficulty;
    public boolean locked;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte((byte) difficulty.ordinal());
        writer.writeBoolean(locked);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DIFFICULTY;
    }
}
