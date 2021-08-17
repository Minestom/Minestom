package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpdateLightPacket implements ServerPacket {

    public int chunkX;
    public int chunkZ;
    //todo make changeable
    public boolean trustEdges = true;

    public long[] skyLightMask = new long[0];
    public long[] blockLightMask = new long[0];

    public long[] emptySkyLightMask = new long[0];
    public long[] emptyBlockLightMask = new long[0];

    public List<byte[]> skyLight = new ArrayList<>();
    public List<byte[]> blockLight = new ArrayList<>();

    /**
     * Default constructor, required for reflection operations.
     */
    public UpdateLightPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);

        writer.writeBoolean(trustEdges);

        writer.writeLongArray(skyLightMask);
        writer.writeLongArray(blockLightMask);

        writer.writeLongArray(emptySkyLightMask);
        writer.writeLongArray(emptyBlockLightMask);

        writer.writeVarInt(skyLight.size());
        for (byte[] bytes : skyLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }

        writer.writeVarInt(blockLight.size());
        for (byte[] bytes : blockLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        chunkX = reader.readVarInt();
        chunkZ = reader.readVarInt();

        trustEdges = reader.readBoolean();

        skyLightMask = reader.readLongArray();
        blockLightMask = reader.readLongArray();

        emptySkyLightMask = reader.readLongArray();
        emptyBlockLightMask = reader.readLongArray();

        // sky light
        skyLight.clear();
        final int skyLightCount = reader.readVarInt();
        for (int i = 0; i < skyLightCount; i++) {
            int length = reader.readVarInt();
            if (length != 2048) {
                throw new IllegalStateException("Length must be 2048.");
            }
            byte[] bytes = reader.readBytes(length);
            skyLight.add(bytes);
        }

        // block light
        blockLight.clear();
        final int blockLightCount = reader.readVarInt();
        for (int i = 0; i < blockLightCount; i++) {
            int length = reader.readVarInt();
            if (length != 2048) {
                throw new IllegalStateException("Length must be 2048.");
            }

            byte[] bytes = reader.readBytes(length);
            blockLight.add(bytes);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_LIGHT;
    }
}
