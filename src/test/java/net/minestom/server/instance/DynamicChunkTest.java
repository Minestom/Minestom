package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.instance.block.predicate.PropertiesPredicate;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public final class DynamicChunkTest {

    @Test
    public void testBlockEntities(Env env) {
        var instance = env.createFlatInstance();
        var blockPosition = new Vec(-64, 40, 64);

        var handler = new BlockHandler() {
            @Override
            public void onPlace(Placement placement) {
                assertEquals(blockPosition, placement.getBlockPosition());
            }

            @Override
            public Key getKey() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(blockPosition, Block.STONE.withHandler(handler));
    }


    public void placeBlockFromAdventureMode(Block baseBlock, BlockPredicates canPlaceOn, Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        instance.setBlock(2, 41, 0, baseBlock);

        player.setGameMode(GameMode.ADVENTURE);
        player.setItemInMainHand(ItemStack.builder(Material.WHITE_WOOL).set(DataComponents.CAN_PLACE_ON, canPlaceOn).build());

        var packet = new ClientPlayerBlockPlacementPacket(
                PlayerHand.MAIN, new Pos(2, 41, 0), BlockFace.WEST,
                1f, 1f, 1f,
                false, false, 0
        );
        player.addPacketToQueue(packet);
        player.interpretPacketQueue();

        var placedBlock = instance.getBlock(1, 41, 0);
        assertEquals("minecraft:white_wool", placedBlock.name());
    }

    private static List<Arguments> chunkBlockEntitiesChunks() {
        final List<Arguments> arguments = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                arguments.add(Arguments.of(x, z));
            }
        }
        return arguments;
    }

    @ParameterizedTest
    @MethodSource("chunkBlockEntitiesChunks")
    public void chunkBlockEntities(int chunkX, int chunkZ, Env env) {
        final Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(DynamicChunk::new);
        final DynamicChunk chunk = (DynamicChunk) instance.loadChunk(chunkX, chunkZ).join();

        final BlockHandler dummyHandler = () -> Key.key("dummy");
        final Map<Point, Block> blockEntities = new HashMap<>();
        final DimensionType dimensionType = instance.getCachedDimensionType();
        for (int y = dimensionType.minY(); y < dimensionType.maxY(); y++) {
            blockEntities.put(new Pos(chunk.chunkX * Chunk.CHUNK_SIZE_X, y, chunk.chunkZ * Chunk.CHUNK_SIZE_Z), Block.STONE.withHandler(dummyHandler));
            blockEntities.put(new Pos(chunk.chunkX * Chunk.CHUNK_SIZE_X + Chunk.CHUNK_SIZE_X - 1, y, chunk.chunkZ * Chunk.CHUNK_SIZE_Z), Block.STONE.withHandler(dummyHandler));
            blockEntities.put(new Pos(chunk.chunkX * Chunk.CHUNK_SIZE_X, y, chunk.chunkZ * Chunk.CHUNK_SIZE_Z + Chunk.CHUNK_SIZE_Z - 1), Block.STONE.withHandler(dummyHandler));
            blockEntities.put(new Pos(chunk.chunkX * Chunk.CHUNK_SIZE_X + Chunk.CHUNK_SIZE_X - 1, y, chunk.chunkZ * Chunk.CHUNK_SIZE_Z + Chunk.CHUNK_SIZE_Z - 1), Block.STONE.withHandler(dummyHandler));
        }

        blockEntities.forEach(instance::setBlock);
        final Map<Point, Block> chunkBlockEntities = chunk.getBlockEntities();
        assertEquals(blockEntities.size(), chunkBlockEntities.size());
        final Collection<Block> blockEntitiesValues = blockEntities.values();
        final Collection<Block> chunkBlockEntitiesValues = chunkBlockEntities.values();
        assertTrue(blockEntitiesValues.containsAll(chunkBlockEntitiesValues));
        assertTrue(chunkBlockEntitiesValues.containsAll(blockEntitiesValues));
    }

}
