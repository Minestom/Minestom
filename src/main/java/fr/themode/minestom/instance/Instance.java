package fr.themode.minestom.instance;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.data.DataContainer;
import fr.themode.minestom.entity.*;
import fr.themode.minestom.instance.batch.BlockBatch;
import fr.themode.minestom.instance.batch.ChunkBatch;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.net.PacketWriterUtils;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.ChunkUtils;
import fr.themode.minestom.utils.Position;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class Instance implements BlockModifier, DataContainer {

    protected static final ChunkLoaderIO CHUNK_LOADER_IO = new ChunkLoaderIO();
    protected static final BlockManager BLOCK_MANAGER = Main.getBlockManager();

    // Entities present in this instance
    protected Set<Player> players = new CopyOnWriteArraySet<>();
    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<ObjectEntity> objectEntities = new CopyOnWriteArraySet<>();
    protected Set<ExperienceOrb> experienceOrbs = new CopyOnWriteArraySet<>();
    // Entities per chunk
    protected Map<Long, Set<Entity>> chunkEntities = new ConcurrentHashMap<>();
    private UUID uniqueId;

    private Data data;

    protected Instance(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    // Used to call BlockBreakEvent and sending particle packet if true
    public abstract void breakBlock(Player player, BlockPosition blockPosition);

    // Force the generation of the chunk, even if no file and ChunkGenerator are defined
    public abstract void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    // Load only if auto chunk load is enabled
    public abstract void loadOptionalChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract Chunk getChunk(int chunkX, int chunkZ);

    public abstract void saveChunkToFolder(Chunk chunk, Runnable callback);

    public abstract void saveChunksToFolder(Runnable callback);

    public abstract BlockBatch createBlockBatch();

    public abstract ChunkBatch createChunkBatch(Chunk chunk);

    public abstract void setChunkGenerator(ChunkGenerator chunkGenerator);

    public abstract Collection<Chunk> getChunks();

    public abstract File getFolder();

    public abstract void setFolder(File folder);

    public abstract void sendChunkUpdate(Player player, Chunk chunk);

    protected abstract void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void sendChunks(Player player);

    public abstract void sendChunk(Player player, Chunk chunk);

    public abstract void enableAutoChunkLoad(boolean enable);

    public abstract boolean hasEnabledAutoChunkLoad();

    //
    protected void sendChunkUpdate(Collection<Player> players, Chunk chunk) {
        Packet chunkData = chunk.getFullDataPacket();
        players.forEach(player -> {
            player.getPlayerConnection().sendPacket(chunkData);
        });
    }

    protected void sendChunkSectionUpdate(Chunk chunk, int section, Collection<Player> players) {
        PacketWriterUtils.writeAndSend(players, getChunkSectionUpdatePacket(chunk, section));
    }

    public void sendChunkSectionUpdate(Chunk chunk, int section, Player player) {
        PacketWriterUtils.writeAndSend(player, getChunkSectionUpdatePacket(chunk, section));
    }

    protected ChunkDataPacket getChunkSectionUpdatePacket(Chunk chunk, int section) {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = false;
        chunkDataPacket.chunk = chunk;
        int[] sections = new int[16];
        sections[section] = 1;
        chunkDataPacket.sections = sections;
        return chunkDataPacket;
    }
    //

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    public Set<ObjectEntity> getObjectEntities() {
        return Collections.unmodifiableSet(objectEntities);
    }

    public Set<ExperienceOrb> getExperienceOrbs() {
        return Collections.unmodifiableSet(experienceOrbs);
    }

    public Set<Entity> getChunkEntities(Chunk chunk) {
        return Collections.unmodifiableSet(getEntitiesInChunk(ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ())));
    }

    public void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    public void loadChunk(Position position, Consumer<Chunk> callback) {
        int chunkX = Math.floorDiv((int) position.getX(), 16);
        int chunkZ = Math.floorDiv((int) position.getY(), 16);
        loadChunk(chunkX, chunkZ, callback);
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

    public Data getBlockData(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getData((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public Chunk getChunkAt(double x, double z) {
        int chunkX = Math.floorDiv((int) x, 16);
        int chunkZ = Math.floorDiv((int) z, 16);
        return getChunk(chunkX, chunkZ);
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    public Chunk getChunkAt(BlockPosition blockPosition) {
        return getChunkAt(blockPosition.getX(), blockPosition.getZ());
    }

    public Chunk getChunkAt(Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    public void saveChunkToFolder(Chunk chunk) {
        saveChunkToFolder(chunk, null);
    }

    public void saveChunksToFolder() {
        saveChunksToFolder(null);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    // UNSAFE METHODS (need most of time to be synchronized)

    public void addEntity(Entity entity) {
        Instance lastInstance = entity.getInstance();
        if (lastInstance != null && lastInstance != this) {
            lastInstance.removeEntity(entity); // If entity is in another instance, remove it from there and add it to this
        }

        long[] visibleChunksEntity = ChunkUtils.getChunksInRange(entity.getPosition(), Main.ENTITY_VIEW_DISTANCE);
        boolean isPlayer = entity instanceof Player;

        if (isPlayer) {
            sendChunks((Player) entity);
        }

        // Send all visible entities
        for (long chunkIndex : visibleChunksEntity) {
            getEntitiesInChunk(chunkIndex).forEach(ent -> {
                if (isPlayer) {
                    ent.addViewer((Player) entity);
                }

                if (ent instanceof Player) {
                    entity.addViewer((Player) ent);
                }
            });
        }

        Chunk chunk = getChunkAt(entity.getPosition());
        addEntityToChunk(entity, chunk);
    }

    public void removeEntity(Entity entity) {
        Instance entityInstance = entity.getInstance();
        if (entityInstance == null || entityInstance != this)
            return;

        entity.getViewers().forEach(p -> entity.removeViewer(p)); // Remove this entity from players viewable list and send delete entities packet

        Chunk chunk = getChunkAt(entity.getPosition());
        removeEntityFromChunk(entity, chunk);
    }

    public void addEntityToChunk(Entity entity, Chunk chunk) {
        long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        synchronized (chunkEntities) {
            Set<Entity> entities = getEntitiesInChunk(chunkIndex);
            entities.add(entity);
            this.chunkEntities.put(chunkIndex, entities);
            if (entity instanceof Player) {
                this.players.add((Player) entity);
            } else if (entity instanceof EntityCreature) {
                this.creatures.add((EntityCreature) entity);
            } else if (entity instanceof ObjectEntity) {
                this.objectEntities.add((ObjectEntity) entity);
            } else if (entity instanceof ExperienceOrb) {
                this.experienceOrbs.add((ExperienceOrb) entity);
            }
        }
    }

    public void removeEntityFromChunk(Entity entity, Chunk chunk) {
        long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        synchronized (chunkEntities) {
            Set<Entity> entities = getEntitiesInChunk(chunkIndex);
            entities.remove(entity);
            if (entities.isEmpty()) {
                this.chunkEntities.remove(chunkIndex);
            } else {
                this.chunkEntities.put(chunkIndex, entities);
            }
            if (entity instanceof Player) {
                this.players.remove(entity);
            } else if (entity instanceof EntityCreature) {
                this.creatures.remove(entity);
            } else if (entity instanceof ObjectEntity) {
                this.objectEntities.remove(entity);
            } else if (entity instanceof ExperienceOrb) {
                this.experienceOrbs.remove(entity);
            }
        }
    }

    private Set<Entity> getEntitiesInChunk(long index) {
        Set<Entity> entities = chunkEntities.get(index);
        return entities != null ? entities : new CopyOnWriteArraySet<>();
    }
}
