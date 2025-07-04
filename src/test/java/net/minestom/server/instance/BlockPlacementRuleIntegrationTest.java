package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.SuspiciousGravelBlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class BlockPlacementRuleIntegrationTest {

    @Test
    public void updateNeighborsTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        env.process().block().registerBlockPlacementRule(new FencePlacementRule(Block.OAK_FENCE));

        connection.connect(instance, new Pos(0, 52, 0));

        instance.setBlock(1, 50, 0, Block.OAK_FENCE);
        instance.setBlock(-1, 50, 0, Block.OAK_FENCE);
        instance.setBlock(0, 50, 1, Block.OAK_FENCE);
        instance.setBlock(0, 50, -1, Block.OAK_FENCE);

        assertEquals(Block.OAK_FENCE, instance.getBlock(1, 50, 0));
        assertEquals(Block.OAK_FENCE, instance.getBlock(-1, 50, 0));
        assertEquals(Block.OAK_FENCE, instance.getBlock(0, 50, 1));
        assertEquals(Block.OAK_FENCE, instance.getBlock(0, 50, -1));

        instance.setBlock(0, 50, 0, Block.OAK_FENCE);

        env.tick();

        assertEquals(Block.OAK_FENCE.withProperties(
                Map.of("north", "true", "south", "true", "west", "true", "east", "true")
        ), instance.getBlock(0, 50, 0));
        assertEquals(Block.OAK_FENCE.withProperties(
                Map.of("north", "false", "south", "false", "west", "true", "east", "false")
        ), instance.getBlock(1, 50, 0));
        assertEquals(Block.OAK_FENCE.withProperties(
                Map.of("north", "false", "south", "false", "west", "false", "east", "true")
        ), instance.getBlock(-1, 50, 0));
        assertEquals(Block.OAK_FENCE.withProperties(
                Map.of("north", "true", "south", "false", "west", "false", "east", "false")
        ), instance.getBlock(0, 50, 1));
        assertEquals(Block.OAK_FENCE.withProperties(
                Map.of("north", "false", "south", "true", "west", "false", "east", "false")
        ), instance.getBlock(0, 50, -1));
    }

    @Test
    public void clientPredictionTest(Env env) {
        var instance = env.createFlatInstance();
        var connection1 = env.createConnection();
        var connection2 = env.createConnection();

        var player1 = connection1.connect(instance, new Pos(0, 50, 0));
        connection2.connect(instance, new Pos(0, 51, 0));

        env.process().block().registerBlockPlacementRule(new BlockPlacementRule(Block.STONE) {
            @Override
            public @NotNull Block blockPlace(@NotNull BlockChange mutation) {
                return mutation.block();
            }
            @Override
            public @NotNull Block blockUpdate(@NotNull BlockChange mutation) {
                return super.blockUpdate(mutation);
            }
            @Override
            public boolean isClientPredicted() {
                return true;
            }
        });

        var tracker1 = connection1.trackIncoming();
        var tracker2 = connection2.trackIncoming();

        instance.placeBlock(new BlockChange.Player(
                instance, new Vec(0, 50, 0), Block.STONE, BlockFace.TOP,
                player1, PlayerHand.MAIN, new Vec(0.5, 0.5, 0.5)
        ), true);

        boolean packetFoundPlayer1 = tracker1.collect().stream().anyMatch(p -> p instanceof BlockChangePacket);
        boolean packetFoundPlayer2 = tracker2.collect().stream().anyMatch(p -> p instanceof BlockChangePacket);

        assertEquals(false, packetFoundPlayer1, "Player 1 should NOT have received a block change packet");
        assertEquals(true, packetFoundPlayer2, "Player 2 should have received a block change packet");
    }

    @Test
    public void clientPredictionTestTwo(Env env) {
        var instance = env.createFlatInstance();
        var connection1 = env.createConnection();
        var connection2 = env.createConnection();

        var player1 = connection1.connect(instance, new Pos(0, 50, 0));
        connection2.connect(instance, new Pos(0, 51, 0));

        env.process().block().registerBlockPlacementRule(new BlockPlacementRule(Block.STONE) {
            @Override
            public @NotNull Block blockPlace(@NotNull BlockChange mutation) {
                return mutation.block();
            }
            @Override
            public @NotNull Block blockUpdate(@NotNull BlockChange mutation) {
                return super.blockUpdate(mutation);
            }
            @Override
            public boolean isClientPredicted() {
                return false;
            }
        });

        var tracker1 = connection1.trackIncoming();
        var tracker2 = connection2.trackIncoming();

        instance.placeBlock(new BlockChange.Player(
                instance, new Vec(0, 50, 0), Block.STONE, BlockFace.TOP,
                player1, PlayerHand.MAIN, new Vec(0.5, 0.5, 0.5)
        ), true);

        boolean packetFoundPlayer1 = tracker1.collect().stream().anyMatch(p -> p instanceof BlockChangePacket);
        boolean packetFoundPlayer2 = tracker2.collect().stream().anyMatch(p -> p instanceof BlockChangePacket);

        assertEquals(true, packetFoundPlayer1, "Player 1 should have received a block change packet");
        assertEquals(true, packetFoundPlayer2, "Player 2 should have received a block change packet");
    }

    @Test
    public void handlerPresentInPlacementRuleUpdate(Env env) {
        AtomicReference<Block> currentBlock = new AtomicReference<>();
        env.process().block().registerHandler(SuspiciousGravelBlockHandler.INSTANCE.getKey(), () -> SuspiciousGravelBlockHandler.INSTANCE);
        env.process().block().registerBlockPlacementRule(new BlockPlacementRule(Block.SUSPICIOUS_GRAVEL) {
            @Override
            public @NotNull Block blockPlace(@NotNull BlockChange mutation) {
                return mutation.block();
            }

            @Override
            public @NotNull Block blockUpdate(@NotNull BlockChange mutation) {
                currentBlock.set(mutation.block());
                return super.blockUpdate(mutation);
            }
        });

        var instance = env.createFlatInstance();
        var theBlock = Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE);
        instance.setBlock(0, 50, 0, theBlock);
        instance.setBlock(1, 50, 0, theBlock);

        assertEquals(theBlock, currentBlock.get());
    }

}

class FencePlacementRule extends BlockPlacementRule {

    public FencePlacementRule(Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull BlockChange blockChange) {
        var instance = blockChange.instance();
        var position = blockChange.blockPosition();

        return calculateConnections(instance, position);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull BlockChange updateState) {
        var instance = updateState.instance();
        var position = updateState.blockPosition();

        return calculateConnections(instance, position);
    }

    @NotNull
    private Block calculateConnections(Block.Getter instance, Point position) {
        Map<String, String> connections = new HashMap<>();

        if (!(instance instanceof Instance realInstance)) return this.block;

        connections.put("north", realInstance.isChunkLoaded(position.add(0, 0, -1)) && realInstance.getBlock(position.add(0, 0, -1)).isSolid() ? "true" : "false");
        connections.put("south", realInstance.isChunkLoaded(position.add(0, 0, 1)) && realInstance.getBlock(position.add(0, 0, 1)).isSolid() ? "true" : "false");
        connections.put("west", realInstance.isChunkLoaded(position.add(-1, 0, 0)) && realInstance.getBlock(position.add(-1, 0, 0)).isSolid() ? "true" : "false");
        connections.put("east", realInstance.isChunkLoaded(position.add(1, 0, 0)) && realInstance.getBlock(position.add(1, 0, 0)).isSolid() ? "true" : "false");

        return block.withProperties(
                connections
        );
    }

    @Override
    public @NotNull @Unmodifiable List<Vec> updateShape() {
        return List.of(
                Direction.NORTH.vec(),
                Direction.SOUTH.vec(),
                Direction.EAST.vec(),
                Direction.WEST.vec()
        );
    }

    @Override
    public boolean considerUpdate(@NotNull Vec offset, @NotNull Block block) {
        // Fences should only consider updates from solid blocks that are next to them
        return super.considerUpdate(offset, block) && block.isSolid() || block.isAir(); // ensure the block is solid
    }
}
