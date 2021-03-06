package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.cache.CacheablePacket;
import net.minestom.server.utils.cache.TemporaryCache;
import net.minestom.server.utils.cache.TimedBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class UpdateLightPacket implements ServerPacket, CacheablePacket {

    private static final TemporaryCache<TimedBuffer> CACHE = new TemporaryCache<>(30000L);

    public int chunkX;
    public int chunkZ;
    //todo make changeable
    public boolean trustEdges = true;

    public int skyLightMask;
    public int blockLightMask;

    public int emptySkyLightMask;
    public int emptyBlockLightMask;

    public List<byte[]> skyLight;
    public List<byte[]> blockLight;

    // Cacheable data
    private final UUID identifier;
    private final long timestamp;

    public UpdateLightPacket(@Nullable UUID identifier, long timestamp) {
        this.identifier = identifier;
        this.timestamp = timestamp;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);

        writer.writeBoolean(trustEdges);

        writer.writeVarInt(skyLightMask);
        writer.writeVarInt(blockLightMask);

        writer.writeVarInt(emptySkyLightMask);
        writer.writeVarInt(emptyBlockLightMask);

        //writer.writeVarInt(skyLight.size());
        for (byte[] bytes : skyLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }

        //writer.writeVarInt(blockLight.size());
        for (byte[] bytes : blockLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_LIGHT;
    }

    @NotNull
    @Override
    public TemporaryCache<TimedBuffer> getCache() {
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
