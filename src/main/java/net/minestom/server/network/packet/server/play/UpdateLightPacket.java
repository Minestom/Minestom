package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.cache.CacheablePacket;
import net.minestom.server.utils.cache.TemporaryPacketCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UpdateLightPacket implements ServerPacket, CacheablePacket {

    public static final TemporaryPacketCache CACHE = new TemporaryPacketCache(5, TimeUnit.MINUTES);

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

    // Cacheable data
    private final UUID identifier;
    private final long timestamp;

    /**
     * Default constructor, required for reflection operations.
     * This one will make a packet that is not meant to be cached
     */
    public UpdateLightPacket() {
        this(UUID.randomUUID(), Long.MAX_VALUE);
    }

    public UpdateLightPacket(@Nullable UUID identifier, long timestamp) {
        this.identifier = identifier;
        this.timestamp = timestamp;
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
        for (int i = 0; i < 14; i++) {
            int length = reader.readVarInt();
            if (length != 2048) {
                throw new IllegalStateException("Length must be 2048.");
            }

            byte[] bytes = reader.readBytes(length);
            skyLight.add(bytes);
        }

        // block light
        blockLight.clear();
        for (int i = 0; i < 6; i++) {
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

    @Override
    public @NotNull TemporaryPacketCache getCache() {
        return CACHE;
    }

    @Override
    public UUID getIdentifier() {
        return identifier;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
