package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.cache.CacheablePacket;
import net.minestom.server.utils.cache.TemporaryCache;
import net.minestom.server.utils.cache.TimedBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UpdateLightPacket implements ServerPacket, CacheablePacket {

    private static final TemporaryCache<TimedBuffer> CACHE = new TemporaryCache<>(5, TimeUnit.MINUTES,
            notification -> notification.getValue().getBuffer().release());

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

    /**
     * Default constructor, required for reflection operations.
     * This one will make a packet that is not meant to be cached
     */
    public UpdateLightPacket() {
        this(UUID.randomUUID(), Long.MAX_VALUE);
        for (int i = 0; i < 14; i++) {
            skyLight.add(new byte[2048]);
        }
        for (int i = 0; i < 6; i++) {
            blockLight.add(new byte[2048]);
        }
    }

    public UpdateLightPacket(@Nullable UUID identifier, long timestamp) {
        this.identifier = identifier;
        this.timestamp = timestamp;
        skyLight = new ArrayList<>(14);
        blockLight = new ArrayList<>(6);
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
    public void read(@NotNull BinaryReader reader) {
        chunkX = reader.readVarInt();
        chunkZ = reader.readVarInt();

        trustEdges = reader.readBoolean();

        skyLightMask = reader.readVarInt();
        blockLightMask = reader.readVarInt();

        emptySkyLightMask = reader.readVarInt();
        emptyBlockLightMask = reader.readVarInt();

        // sky light
        skyLight.clear();
        for (int i = 0; i < 14; i++) {
            int length = reader.readVarInt();
            if(length != 2048) {
                throw new IllegalStateException("Length must be 2048.");
            }

            byte[] bytes = reader.readBytes(length);
            skyLight.add(bytes);
        }

        // block light
        blockLight.clear();
        for (int i = 0; i < 6; i++) {
            int length = reader.readVarInt();
            if(length != 2048) {
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
