package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;

public class RespawnPacket implements ServerPacket {

    public DimensionType dimensionType;
    public long hashedSeed;
    public GameMode gameMode;
    public boolean isDebug = false;
    public boolean isFlat = true;
    public boolean copyMeta = true;

    public RespawnPacket() {
        dimensionType = DimensionType.OVERWORLD;
        gameMode = GameMode.SURVIVAL;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeNBT("", dimensionType.toNBT());

        // Warning: must be different for each dimension type! Otherwise the client seems to cache the world name
        writer.writeSizedString(dimensionType.getName().toString());

        writer.writeLong(hashedSeed);
        writer.writeByte(gameMode.getId());
        writer.writeByte(gameMode.getId()); // Hardcore flag not included
        writer.writeBoolean(isDebug);
        writer.writeBoolean(isFlat);
        writer.writeBoolean(copyMeta);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        try {
            dimensionType = DimensionType.fromNBT((NBTCompound) reader.readTag());

            // dimension type name
            reader.readSizedString();

            hashedSeed = reader.readLong();
            gameMode = GameMode.values()[reader.readByte()];
            // TODO: hardcore flag
            reader.readByte();
            isDebug = reader.readBoolean();
            isFlat = reader.readBoolean();
            copyMeta = reader.readBoolean();
        } catch (IOException | NBTException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read DimensionType inside RespawnPacket", e);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESPAWN;
    }
}
