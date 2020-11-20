package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.cache.CacheablePacket;
import net.minestom.server.utils.cache.TemporaryPacketCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class UpdateLightPacket implements ServerPacket, CacheablePacket {

    private static final TemporaryPacketCache CACHE = new TemporaryPacketCache(10000L);

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
    private UUID identifier;
    private long lastUpdate;

    public UpdateLightPacket(@Nullable UUID identifier, long lastUpdate) {
        this.identifier = identifier;
        this.lastUpdate = lastUpdate;
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

    @Override
    public TemporaryPacketCache getCache() {
        return CACHE;
    }

    @Override
    public UUID getIdentifier() {
        return identifier;
    }

    @Override
    public long getLastUpdateTime() {
        return lastUpdate;
    }
}
