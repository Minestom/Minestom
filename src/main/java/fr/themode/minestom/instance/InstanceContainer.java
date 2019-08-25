package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerBlockBreakEvent;
import fr.themode.minestom.net.PacketWriter;
import fr.themode.minestom.net.packet.server.play.ParticlePacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.ChunkUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * InstanceContainer is an instance that contains chunks in contrary to SharedInstance.
 */
public class InstanceContainer extends Instance {

    private File folder;

    private List<SharedInstance> sharedInstances = new CopyOnWriteArrayList<>();

    private ChunkGenerator chunkGenerator;
    private Map<Long, Chunk> chunks = new ConcurrentHashMap<>();

    private boolean autoChunkLoad;

    protected InstanceContainer(UUID uniqueId, File folder) {
        super(uniqueId);
        this.folder = folder;
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, short blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            PacketWriter.writeCallbackPacket(chunk.getFreshPartialDataPacket(), buffer -> {
                chunk.setFullDataPacket(buffer);
                sendChunkUpdate(chunk);
            });
        }
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, String blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setCustomBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            PacketWriter.writeCallbackPacket(chunk.getFreshPartialDataPacket(), buffer -> {
                chunk.setFullDataPacket(buffer);
                sendChunkUpdate(chunk);
            });
        }
    }

    // TODO deplace
    @Override
    public void breakBlock(Player player, BlockPosition blockPosition) {
        Chunk chunk = getChunkAt(blockPosition);
        short blockId = chunk.getBlockId((byte) (blockPosition.getX() % 16), (byte) blockPosition.getY(), (byte) (blockPosition.getZ() % 16));
        if (blockId == 0) {
            sendChunkUpdate(player, chunk);
            return;
        }

        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(blockPosition);
        player.callEvent(PlayerBlockBreakEvent.class, blockBreakEvent);
        if (!blockBreakEvent.isCancelled()) {
            // TODO blockbreak setBlock result
            int x = blockPosition.getX();
            int y = blockPosition.getY();
            int z = blockPosition.getZ();
            setBlock(x, y, z, (short) 0);
            ParticlePacket particlePacket = new ParticlePacket(); // TODO change to a proper particle API
            particlePacket.particleId = 3; // Block particle
            particlePacket.longDistance = false;
            particlePacket.x = x + 0.5f;
            particlePacket.y = y;
            particlePacket.z = z + 0.5f;
            particlePacket.offsetX = 0.45f;
            particlePacket.offsetY = 0.55f;
            particlePacket.offsetZ = 0.45f;
            particlePacket.particleData = 0.3f;
            particlePacket.particleCount = 100;
            particlePacket.blockId = blockId;
            player.getPlayerConnection().sendPacket(particlePacket);
            player.sendPacketToViewers(particlePacket);
        } else {
            sendChunkUpdate(player, chunk);
        }
    }

    @Override
    public void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            if (callback != null)
                callback.accept(chunk);
        } else {
            retrieveChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    public void loadOptionalChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            if (callback != null)
                callback.accept(chunk);
        } else {
            if (hasEnabledAutoChunkLoad()) {
                retrieveChunk(chunkX, chunkZ, callback);
            } else {
                callback.accept(null);
            }
        }
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    @Override
    public void saveToFolder(Runnable callback) {
        if (folder == null)
            throw new UnsupportedOperationException("You cannot save an instance without setting a folder.");

        Iterator<Chunk> chunks = getChunks().iterator();
        while (chunks.hasNext()) {
            Chunk chunk = chunks.next();
            boolean isLast = !chunks.hasNext();
            CHUNK_LOADER_IO.saveChunk(chunk, getFolder(), isLast ? callback : null);
        }
    }

    @Override
    public BlockBatch createBlockBatch() {
        return new BlockBatch(this);
    }

    @Override
    public ChunkBatch createChunkBatch(Chunk chunk) {
        return new ChunkBatch(this, chunk);
    }

    @Override
    public void sendChunkUpdate(Player player, Chunk chunk) {
        Buffer chunkData = chunk.getFullDataPacket();
        chunkData.getData().retain(1).markReaderIndex();
        player.getPlayerConnection().sendUnencodedPacket(chunkData);
        chunkData.getData().resetReaderIndex();
    }

    @Override
    protected void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        if (folder != null) {
            // Load from file if possible
            CHUNK_LOADER_IO.loadChunk(chunkX, chunkZ, this, chunk -> {
                cacheChunk(chunk);
                if (callback != null)
                    callback.accept(chunk);
            });
        } else {
            // Folder isn't defined, create new chunk
            createChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    public void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        Biome biome = chunkGenerator != null ? chunkGenerator.getBiome(chunkX, chunkZ) : Biome.VOID;
        Chunk chunk = new Chunk(biome, chunkX, chunkZ);
        cacheChunk(chunk);
        if (chunkGenerator != null) {
            ChunkBatch chunkBatch = createChunkBatch(chunk);
            chunkGenerator.generateChunkData(chunkBatch, chunkX, chunkZ);
            chunkBatch.flush(callback);
        }
    }

    public void sendChunkUpdate(Chunk chunk) {

        // Update for players in this instance
        if (!getPlayers().isEmpty())
            sendChunkUpdate(getPlayers(), chunk);

        // Update for shared instances
        if (!sharedInstances.isEmpty())
            this.sharedInstances.forEach(sharedInstance -> {
                Set<Player> instancePlayers = sharedInstance.getPlayers();
                if (!instancePlayers.isEmpty())
                    sendChunkUpdate(instancePlayers, chunk);
            });
    }

    @Override
    public void sendChunks(Player player) {
        for (Chunk chunk : getChunks()) {
            sendChunk(player, chunk);
        }
    }

    @Override
    public void sendChunk(Player player, Chunk chunk) {
        Buffer chunkData = chunk.getFullDataPacket();
        if (chunkData == null) {
            PacketWriter.writeCallbackPacket(chunk.getFreshFullDataPacket(), buffer -> {
                buffer.getData().retain(1).markReaderIndex();
                player.getPlayerConnection().sendUnencodedPacket(buffer);
                buffer.getData().resetReaderIndex();
                chunk.setFullDataPacket(buffer);
            });
        } else {
            chunkData.getData().retain(1).markReaderIndex();
            player.getPlayerConnection().sendUnencodedPacket(chunkData);
            chunkData.getData().resetReaderIndex();
        }
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        this.autoChunkLoad = enable;
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return autoChunkLoad;
    }

    protected void addSharedInstance(SharedInstance sharedInstance) {
        this.sharedInstances.add(sharedInstance);
    }

    private void cacheChunk(Chunk chunk) {
        this.chunks.put(ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ()), chunk);
    }

    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public Collection<Chunk> getChunks() {
        return Collections.unmodifiableCollection(chunks.values());
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

}
