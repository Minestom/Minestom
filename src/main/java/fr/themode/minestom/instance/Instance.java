package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.ObjectEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.BlockBreakEvent;
import fr.themode.minestom.net.PacketWriter;
import fr.themode.minestom.net.packet.server.play.DestroyEntitiesPacket;
import fr.themode.minestom.net.packet.server.play.ParticlePacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.GroupedCollections;
import fr.themode.minestom.utils.Position;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Instance implements BlockModifier {

    private static ChunkLoaderIO chunkLoaderIO = new ChunkLoaderIO();

    private UUID uniqueId;
    private File folder;

    private GroupedCollections<ObjectEntity> objectEntities = new GroupedCollections<>(new CopyOnWriteArrayList<>());
    private GroupedCollections<EntityCreature> creatures = new GroupedCollections<>(new CopyOnWriteArrayList());
    private GroupedCollections<Player> players = new GroupedCollections<>(new CopyOnWriteArrayList());

    private ChunkGenerator chunkGenerator;
    private Map<Long, Chunk> chunks = new ConcurrentHashMap<>();

    public Instance(UUID uniqueId, File folder) {
        this.uniqueId = uniqueId;
        this.folder = folder;
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, short blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            chunk.refreshDataPacket();
            sendChunkUpdate(chunk);
            /*PacketWriter.writeCallbackPacket(chunk.getFreshFullDataPacket(), buffer -> {
                chunk.setFullDataPacket(buffer);
                sendChunkUpdate(chunk);
            });*/
        }
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, String blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            chunk.refreshDataPacket();
            sendChunkUpdate(chunk);
            /*PacketWriter.writeCallbackPacket(chunk.getFreshFullDataPacket(), buffer -> {
                chunk.setFullDataPacket(buffer);
                sendChunkUpdate(chunk);
            });*/
        }
    }

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

    public void breakBlock(Player player, BlockPosition blockPosition, CustomBlock customBlock) {
        breakBlock(player, blockPosition, customBlock.getType());
    }

    public void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            if (callback != null)
                callback.accept(chunk);
        } else {
            retrieveChunk(chunkX, chunkZ, callback);
        }
    }

    public void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    public void loadChunk(Position position, Consumer<Chunk> callback) {
        int chunkX = Math.floorDiv((int) position.getX(), 16);
        int chunkZ = Math.floorDiv((int) position.getY(), 16);
        loadChunk(chunkX, chunkZ, callback);
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    public short getBlockId(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getBlockId((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public short getBlockId(BlockPosition blockPosition) {
        return getBlockId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getCustomBlock((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public BlockBatch createBlockBatch() {
        return new BlockBatch(this);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(getChunkKey(chunkX, chunkZ));
    }

    public Chunk getChunkAt(double x, double z) {
        int chunkX = Math.floorDiv((int) x, 16);
        int chunkZ = Math.floorDiv((int) z, 16);
        return getChunk(chunkX, chunkZ);
    }

    public Chunk getChunkAt(BlockPosition blockPosition) {
        return getChunkAt(blockPosition.getX(), blockPosition.getZ());
    }

    public Chunk getChunkAt(Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    public void saveToFolder(Runnable callback) {
        if (folder == null)
            throw new UnsupportedOperationException("You cannot save an instance without specified folder.");

        Iterator<Chunk> chunks = getChunks().iterator();
        while (chunks.hasNext()) {
            Chunk chunk = chunks.next();
            boolean isLast = !chunks.hasNext();
            chunkLoaderIO.saveChunk(chunk, getFolder(), isLast ? callback : null);
        }
    }

    public void saveToFolder() {
        saveToFolder(null);
    }


    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public Collection<Chunk> getChunks() {
        return Collections.unmodifiableCollection(chunks.values());
    }

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
    }

    public GroupedCollections<ObjectEntity> getObjectEntities() {
        return objectEntities;
    }

    public GroupedCollections<EntityCreature> getCreatures() {
        return creatures;
    }

    public GroupedCollections<Player> getPlayers() {
        return players;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public void sendChunkUpdate(Player player, Chunk chunk) {
        Buffer chunkData = chunk.getFullDataPacket();
        chunkData.getData().retain(1).markReaderIndex();
        player.getPlayerConnection().sendUnencodedPacket(chunkData);
        chunkData.getData().resetReaderIndex();
    }

    protected void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        if (folder != null) {
            // Load from file if possible
            chunkLoaderIO.loadChunk(chunkX, chunkZ, this, chunk -> {
                cacheChunk(chunk);
                if (callback != null)
                    callback.accept(chunk);
            });
        } else {
            createChunk(chunkX, chunkZ, callback);
        }
    }

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

    private void cacheChunk(Chunk chunk) {
        this.objectEntities.addCollection(chunk.objectEntities);
        this.creatures.addCollection(chunk.creatures);
        this.players.addCollection(chunk.players);
        this.chunks.put(getChunkKey(chunk.getChunkX(), chunk.getChunkZ()), chunk);
    }

    protected ChunkBatch createChunkBatch(Chunk chunk) {
        return new ChunkBatch(this, chunk);
    }

    protected void sendChunkUpdate(Chunk chunk) {
        if (getPlayers().isEmpty())
            return;

        Buffer chunkData = chunk.getFullDataPacket();
        chunkData.getData().retain(getPlayers().size()).markReaderIndex();
        getPlayers().forEach(player -> {
            player.getPlayerConnection().sendUnencodedPacket(chunkData);
            chunkData.getData().resetReaderIndex();
        });
    }

    private void sendChunks(Player player) {
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

    private long getChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }
}
