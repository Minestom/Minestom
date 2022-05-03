package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

public record ServerDifficultyPacket(@NotNull Difficulty difficulty, boolean locked) implements ServerPacket {
    public ServerDifficultyPacket(BinaryReader reader) {
        this(Difficulty.values()[reader.readByte()], reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte((byte) difficulty.ordinal());
        writer.writeBoolean(locked);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DIFFICULTY;
    }
}
