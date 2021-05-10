package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

public class ServerDifficultyPacket implements ServerPacket {

    public Difficulty difficulty;
    public boolean locked;

    public ServerDifficultyPacket() {
        difficulty = Difficulty.NORMAL;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte((byte) difficulty.ordinal());
        writer.writeBoolean(locked);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        difficulty = Difficulty.values()[reader.readByte()];
        locked = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DIFFICULTY;
    }
}
