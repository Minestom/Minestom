package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.world.Difficulty;

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
