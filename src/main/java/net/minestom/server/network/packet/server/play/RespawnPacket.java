package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public record RespawnPacket(DimensionType dimensionType, String worldName,
                            long hashedSeed, GameMode gameMode, GameMode previousGameMode,
                            boolean isDebug, boolean isFlat, boolean copyMeta) implements ServerPacket {
    public RespawnPacket(BinaryReader reader) {
        this(DimensionType.fromNBT((NBTCompound) reader.readTag()), reader.readSizedString(),
                reader.readLong(), GameMode.values()[reader.readByte()], GameMode.values()[reader.readByte()],
                reader.readBoolean(), reader.readBoolean(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeNBT("", dimensionType.toNBT());
        writer.writeSizedString(worldName);
        writer.writeLong(hashedSeed);
        writer.writeByte(gameMode.getId());
        writer.writeByte(previousGameMode.getId()); // Hardcore flag not included
        writer.writeBoolean(isDebug);
        writer.writeBoolean(isFlat);
        writer.writeBoolean(copyMeta);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESPAWN;
    }
}
