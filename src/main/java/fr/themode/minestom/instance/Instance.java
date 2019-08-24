package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.ObjectEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.GroupedCollections;
import fr.themode.minestom.utils.Position;

import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public interface Instance extends BlockModifier {

    ChunkLoaderIO CHUNK_LOADER_IO = new ChunkLoaderIO();

    void breakBlock(Player player, BlockPosition blockPosition, short blockId);

    void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    Chunk getChunk(int chunkX, int chunkZ);

    void saveToFolder(Runnable callback);

    BlockBatch createBlockBatch();

    ChunkBatch createChunkBatch(Chunk chunk);

    void setChunkGenerator(ChunkGenerator chunkGenerator);

    Collection<Chunk> getChunks();

    GroupedCollections<ObjectEntity> getObjectEntities();

    GroupedCollections<EntityCreature> getCreatures();

    GroupedCollections<Player> getPlayers();

    UUID getUniqueId();

    File getFolder();

    void setFolder(File folder);

    void sendChunkUpdate(Player player, Chunk chunk);

    void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    void sendChunks(Player player);

    SharedInstance createSharedInstance();

    //

    default void sendChunkUpdate(Iterable<Player> players, Chunk chunk) {
        Buffer chunkData = chunk.getFullDataPacket();
        chunkData.getData().retain(getPlayers().size()).markReaderIndex();
        players.forEach(player -> {
            player.getPlayerConnection().sendUnencodedPacket(chunkData);
            chunkData.getData().resetReaderIndex();
        });
    }

    //

    default void breakBlock(Player player, BlockPosition blockPosition, CustomBlock customBlock) {
        breakBlock(player, blockPosition, customBlock.getType());
    }

    default void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    default void loadChunk(Position position, Consumer<Chunk> callback) {
        int chunkX = Math.floorDiv((int) position.getX(), 16);
        int chunkZ = Math.floorDiv((int) position.getY(), 16);
        loadChunk(chunkX, chunkZ, callback);
    }

    default short getBlockId(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getBlockId((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    default short getBlockId(BlockPosition blockPosition) {
        return getBlockId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    default CustomBlock getCustomBlock(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getCustomBlock((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    default Chunk getChunkAt(double x, double z) {
        int chunkX = Math.floorDiv((int) x, 16);
        int chunkZ = Math.floorDiv((int) z, 16);
        return getChunk(chunkX, chunkZ);
    }

    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    default Chunk getChunkAt(BlockPosition blockPosition) {
        return getChunkAt(blockPosition.getX(), blockPosition.getZ());
    }

    default Chunk getChunkAt(Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    default void saveToFolder() {
        saveToFolder(null);
    }

    default long getChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    // UNSAFE METHODS
    void addEntity(Entity entity);

    void removeEntity(Entity entity);
}
