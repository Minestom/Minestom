package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.BlockBreakEvent;
import fr.themode.minestom.net.PacketWriter;
import fr.themode.minestom.net.packet.server.play.ParticlePacket;
import fr.themode.minestom.utils.BlockPosition;

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

    protected InstanceContainer(UUID uniqueId, File folder) {
        super(uniqueId);
        this.folder = folder;
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, short blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            PacketWriter.writeCallbackPacket(chunk.getFreshFullDataPacket(), buffer -> {
                chunk.setFullDataPacket(buffer);
                sendChunkUpdate(chunk);
            });
        }
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, String blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            PacketWriter.writeCallbackPacket(chunk.getFreshFullDataPacket(), buffer -> {
                chunk.setFullDataPacket(buffer);
                sendChunkUpdate(chunk);
            });
        }
    }

    // TODO deplace
    @Override
    public void breakBlock(Player player, BlockPosition blockPosition, short blockId) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(blockPosition);
        player.callEvent(BlockBreakEvent.class, blockBreakEvent);
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
            particlePacket.offsetX = 0.4f;
            particlePacket.offsetY = 0.6f;
            particlePacket.offsetZ = 0.4f;
            particlePacket.particleData = 0.3f;
            particlePacket.particleCount = 75;
            particlePacket.blockId = blockId;
            player.getPlayerConnection().sendPacket(particlePacket);
            player.sendPacketToViewers(particlePacket);
        } else {
            sendChunkUpdate(player, getChunkAt(blockPosition));
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
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(getChunkIndex(chunkX, chunkZ));
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

    /*@Override
    public void addEntity(Entity entity) {
        Instance lastInstance = entity.getInstance();
        if (lastInstance != null && lastInstance != this) {
            lastInstance.removeEntity(entity);
        }

        // TODO based on distance with players
        getPlayers().forEach(p -> entity.addViewer(p));

        if (entity instanceof Player) {
            Player player = (Player) entity;
            sendChunks(player);
            getObjectEntities().forEach(objectEntity -> objectEntity.addViewer(player));
            getCreatures().forEach(entityCreature -> entityCreature.addViewer(player));
            getPlayers().forEach(p -> p.addViewer(player));
        }

        Chunk chunk = getChunkAt(entity.getPosition());
        chunk.addEntity(entity);
    }

    @Override
    public void removeEntity(Entity entity) {
        Instance entityInstance = entity.getInstance();
        if (entityInstance == null || entityInstance != this)
            return;

        entity.getViewers().forEach(p -> entity.removeViewer(p));

        if (!(entity instanceof Player)) {
            DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
            destroyEntitiesPacket.entityIds = new int[]{entity.getEntityId()};

            entity.getViewers().forEach(p -> p.getPlayerConnection().sendPacket(destroyEntitiesPacket)); // TODO destroy batch
        } else {
            // TODO optimize (cache all entities that the player see)
            Player player = (Player) entity;
            getObjectEntities().forEach(objectEntity -> objectEntity.removeViewer(player));
            getCreatures().forEach(entityCreature -> entityCreature.removeViewer(player));
            getPlayers().forEach(p -> p.removeViewer(player));

        }

        Chunk chunk = getChunkAt(entity.getPosition());
        chunk.removeEntity(entity);
    }*/

    @Override
    public void sendChunkUpdate(Player player, Chunk chunk) {
        Buffer chunkData = chunk.getFullDataPacket();
        chunkData.getData().retain(1).markReaderIndex();
        player.getPlayerConnection().sendUnencodedPacket(chunkData);
        chunkData.getData().resetReaderIndex();
    }

    @Override
    public void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        if (folder != null) {
            // Load from file if possible
            CHUNK_LOADER_IO.loadChunk(chunkX, chunkZ, this, chunk -> {
                cacheChunk(chunk);
                if (callback != null)
                    callback.accept(chunk);
            });
        } else {
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
        this.sharedInstances.forEach(sharedInstance -> {
            if (!sharedInstance.getPlayers().isEmpty())
                sendChunkUpdate(sharedInstance.getPlayers(), chunk);
        });
    }

    @Override
    public void sendChunks(Player player) {
        for (Chunk chunk : getChunks()) {
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
    }

    protected void addSharedInstance(SharedInstance sharedInstance) {
        this.sharedInstances.add(sharedInstance);
    }

    private void cacheChunk(Chunk chunk) {
        //this.objectEntities.addCollection(chunk.objectEntities);
        //this.creatures.addCollection(chunk.creatures);
        //this.players.addCollection(chunk.players);
        this.chunks.put(getChunkIndex(chunk.getChunkX(), chunk.getChunkZ()), chunk);
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
