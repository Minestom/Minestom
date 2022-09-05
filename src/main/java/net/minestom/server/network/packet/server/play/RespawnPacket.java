package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record RespawnPacket(String dimensionType, String worldName,
                            long hashedSeed, GameMode gameMode, GameMode previousGameMode,
                            boolean isDebug, boolean isFlat, boolean copyMeta) implements ServerPacket {
    public RespawnPacket(BinaryReader reader) {
        this(reader.readSizedString(), reader.readSizedString(),
                reader.readLong(), GameMode.values()[reader.readByte()], GameMode.values()[reader.readByte()],
                reader.readBoolean(), reader.readBoolean(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(dimensionType);
        writer.writeSizedString(worldName);
        writer.writeLong(hashedSeed);
        writer.writeByte(gameMode.id());
        writer.writeByte(previousGameMode.id());
        writer.writeBoolean(isDebug);
        writer.writeBoolean(isFlat);
        writer.writeBoolean(copyMeta);

        writer.writeBoolean(false);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESPAWN;
    }
}
