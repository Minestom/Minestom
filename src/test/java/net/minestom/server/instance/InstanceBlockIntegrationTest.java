package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.SuspiciousGravelBlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.tag.Tag;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InstanceBlockIntegrationTest {

    @Test
    public void basic(Env env) {
        var instance = env.createFlatInstance();
        assertThrows(NullPointerException.class, () -> instance.getBlock(0, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");

        instance.loadChunk(0, 0).join();
        assertEquals(Block.AIR, instance.getBlock(0, 50, 0));

        instance.setBlock(0, 50, 0, Block.GRASS_BLOCK);
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, 50, 0));

        instance.setBlock(0, 50, 0, Block.STONE);
        assertEquals(Block.STONE, instance.getBlock(0, 50, 0));

        assertThrows(NullPointerException.class, () -> instance.getBlock(16, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");
        instance.loadChunk(1, 0).join();
        assertEquals(Block.AIR, instance.getBlock(16, 50, 0));
    }

    @Test
    public void unloadCache(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        instance.setBlock(0, 50, 0, Block.GRASS_BLOCK);
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, 50, 0));

        instance.unloadChunk(0, 0);
        assertThrows(NullPointerException.class, () -> instance.getBlock(0, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");

        instance.loadChunk(0, 0).join();
        assertEquals(Block.AIR, instance.getBlock(0, 50, 0));
    }

    @Test
    public void blockNbt(Env env) {
        var instance = env.createFlatInstance();
        assertThrows(NullPointerException.class, () -> instance.getBlock(0, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");

        instance.loadChunk(0, 0).join();

        var tag = Tag.Integer("key");
        var block = Block.STONE.withTag(tag, 5);
        var point = new Vec(0, 50, 0);
        // Initial placement
        instance.setBlock(point, block);
        assertEquals(5, instance.getBlock(point).getTag(tag));

        // Override
        instance.setBlock(point, block.withTag(tag, 7));
        assertEquals(7, instance.getBlock(point).getTag(tag));

        // Different block type
        instance.setBlock(point, Block.GRASS_BLOCK.withTag(tag, 8));
        assertEquals(8, instance.getBlock(point).getTag(tag));
    }

    @Test
    public void handlerPresentInPlacementRuleUpdate(Env env) {

        AtomicReference<Block> currentBlock = new AtomicReference<>();
        env.process().block().registerHandler(SuspiciousGravelBlockHandler.INSTANCE.getKey(), () -> SuspiciousGravelBlockHandler.INSTANCE);
        env.process().block().registerBlockPlacementRule(new BlockPlacementRule(Block.SUSPICIOUS_GRAVEL) {
            @Override
            public @Nullable Block blockPlace(PlacementState placementState) {
                return block;
            }

            @Override
            public Block blockUpdate(UpdateState updateState) {
                currentBlock.set(updateState.currentBlock());
                return super.blockUpdate(updateState);
            }
        });

        var instance = env.createFlatInstance();
        var theBlock = Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE);
        instance.setBlock(0, 50, 0, theBlock);
        instance.setBlock(1, 50, 0, theBlock);

        assertEquals(theBlock, currentBlock.get());
    }

    @Test
    public void neighborOrder(Env env) {
        List<Point> notified = new ArrayList<>();
        var handler = neighborHandler(change -> notified.add(change.getBlockPosition()));
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var center = new BlockVec(8, 50, 8);
        for (var face : BlockFace.values()) instance.setBlock(center.relative(face), Block.STONE.withHandler(handler));
        notified.clear();

        instance.setBlock(center, Block.GRASS_BLOCK);

        assertEquals(List.of(new BlockVec(7, 50, 8), new BlockVec(9, 50, 8), new BlockVec(8, 49, 8),
                new BlockVec(8, 51, 8), new BlockVec(8, 50, 7), new BlockVec(8, 50, 9)), notified);
    }

    @Test
    public void neighborChangeDetails(Env env) {
        AtomicReference<BlockHandler.NeighborChange> seen = new AtomicReference<>();
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        instance.setBlock(7, 50, 8, Block.STONE.withHandler(neighborHandler(seen::set)));

        instance.setBlock(8, 50, 8, Block.GRASS_BLOCK);

        var change = seen.get();
        assertEquals(instance, change.getInstance());
        assertEquals(Block.STONE, change.getBlock().withHandler(null));
        assertEquals(new BlockVec(7, 50, 8), change.getBlockPosition());
        assertEquals(BlockFace.EAST, change.getBlockFace());
        assertEquals(new BlockVec(8, 50, 8), change.getChangedPosition());
        assertEquals(Block.AIR, change.getPreviousBlock());
        assertEquals(Block.GRASS_BLOCK, change.getChangedBlock());
    }

    @Test
    public void neighborsBeforeShapes(Env env) {
        List<String> events = new ArrayList<>();
        env.process().block().registerBlockPlacementRule(new BlockPlacementRule(Block.STONE) {
            @Override
            public @Nullable Block blockPlace(PlacementState placementState) {
                return placementState.block();
            }

            @Override
            public Block blockUpdate(UpdateState updateState) {
                events.add("shape " + updateState.blockPosition().blockX() + "," + updateState.blockPosition().blockY() + "," + updateState.blockPosition().blockZ());
                return updateState.currentBlock();
            }
        });
        var handler = neighborHandler(change -> events.add("neighbor " + change.getBlockPosition().blockX() + "," + change.getBlockPosition().blockY() + "," + change.getBlockPosition().blockZ()));
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var center = new BlockVec(8, 50, 8);
        for (var face : BlockFace.values()) instance.setBlock(center.relative(face), Block.STONE.withHandler(handler));
        events.clear();

        instance.setBlock(center, Block.GRASS_BLOCK);

        assertEquals(List.of(
                "neighbor 7,50,8", "neighbor 9,50,8", "neighbor 8,49,8", "neighbor 8,51,8", "neighbor 8,50,7", "neighbor 8,50,9",
                "shape 7,50,8", "shape 9,50,8", "shape 8,50,7", "shape 8,50,9", "shape 8,49,8", "shape 8,51,8"), events);
    }

    @Test
    public void noUpdatesNoNeighbors(Env env) {
        List<Point> notified = new ArrayList<>();
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        instance.setBlock(7, 50, 8, Block.STONE.withHandler(neighborHandler(change -> notified.add(change.getBlockPosition()))));

        instance.setBlock(new BlockVec(8, 50, 8), Block.GRASS_BLOCK, false);

        assertTrue(notified.isEmpty());
    }

    @Test
    public void nestedChangesRunFirst(Env env) {
        List<String> events = new ArrayList<>();
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var center = new BlockVec(8, 50, 8);
        var far = new BlockVec(8, 60, 8);
        var recorder = neighborHandler(change -> events.add(change.getBlockPosition().blockX() + "," + change.getBlockPosition().blockY() + "," + change.getBlockPosition().blockZ()));
        for (var face : BlockFace.values()) instance.setBlock(far.relative(face), Block.STONE.withHandler(recorder));
        for (var face : BlockFace.values()) instance.setBlock(center.relative(face), Block.STONE.withHandler(recorder));
        instance.setBlock(center.relative(BlockFace.WEST), Block.STONE.withHandler(neighborHandler(_ -> {
            events.add("west");
            instance.setBlock(far, Block.GRASS_BLOCK);
        })));
        events.clear();

        instance.setBlock(center, Block.GRASS_BLOCK);

        assertEquals(List.of("west", "7,60,8", "9,60,8", "8,59,8", "8,61,8", "8,60,7", "8,60,9",
                "9,50,8", "8,49,8", "8,51,8", "8,50,7", "8,50,9"), events);
    }

    @Test
    public void chainStopsAtTheDistance(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        BlockHandler domino = neighborHandler(change -> {
            if (change.getChangedBlock().compare(Block.GRASS_BLOCK))
                instance.setBlock(change.getBlockPosition().add(1, 0, 0), Block.GRASS_BLOCK);
        }, 3);
        for (int x = 1; x <= 15; x += 2) instance.setBlock(x, 50, 8, Block.STONE.withHandler(domino));

        instance.setBlock(0, 50, 8, Block.GRASS_BLOCK);

        assertEquals(Block.GRASS_BLOCK, instance.getBlock(6, 50, 8));
        assertEquals(Block.AIR, instance.getBlock(8, 50, 8));
    }

    @Test
    public void zeroDistanceSkipsTheHandler(Env env) {
        List<Point> notified = new ArrayList<>();
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        instance.setBlock(7, 50, 8, Block.STONE.withHandler(neighborHandler(change -> notified.add(change.getBlockPosition()), 0)));

        instance.setBlock(8, 50, 8, Block.GRASS_BLOCK);

        assertTrue(notified.isEmpty());
    }

    private static BlockHandler neighborHandler(Consumer<BlockHandler.NeighborChange> onChange) {
        return neighborHandler(onChange, BlockPlacementRule.DEFAULT_UPDATE_RANGE);
    }

    private static BlockHandler neighborHandler(Consumer<BlockHandler.NeighborChange> onChange, int maxDistance) {
        return new BlockHandler() {
            @Override
            public void onNeighborChange(NeighborChange change) {
                onChange.accept(change);
            }

            @Override
            public int maxNeighborUpdateDistance() {
                return maxDistance;
            }

            @Override
            public Key getKey() {
                return Key.key("minestom:neighbor_test");
            }
        };
    }
}
