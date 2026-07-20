package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class BlockActionsIntegrationTest {

    /**
     * A map-backed overlay world: reads fall through to the instance, writes stay in the map - the pipeline
     * runs against it without touching the instance's blocks.
     */
    private static final class OverlayWorld implements BlockActions.World {
        final Instance instance;
        final Map<Point, Block> overlay = new HashMap<>();

        OverlayWorld(Instance instance) {
            this.instance = instance;
        }

        @Override
        public Instance instance() {
            return instance;
        }

        @Override
        public Block getBlock(int x, int y, int z, Condition condition) {
            Block block = overlay.get(new Vec(x, y, z));
            return block != null ? block : instance.getBlock(x, y, z, condition);
        }

        @Override
        public void placeBlock(BlockHandler.PlayerPlacement placement, boolean doBlockUpdates) {
            overlay.put(key(placement.getBlockPosition()), placement.getBlock());
        }

        @Override
        public boolean breakBlock(Player player, Point position, BlockFace blockFace) {
            overlay.put(key(position), Block.AIR);
            return true;
        }

        // one key type: Point implementations do not equal each other across types
        private static Vec key(Point point) {
            return new Vec(point.blockX(), point.blockY(), point.blockZ());
        }
    }

    @Test
    public void placeIntoOverlayWorld(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        player.setItemInMainHand(ItemStack.of(Material.WHITE_WOOL, 64));

        var world = new OverlayWorld(instance);
        world.overlay.put(new Vec(2, 41, 0), Block.STONE); // support exists only in the overlay

        BlockActions.place(world, player, new ClientPlayerBlockPlacementPacket(
                PlayerHand.MAIN, new Pos(2, 41, 0), BlockFace.WEST,
                1f, 1f, 1f,
                false, false, 0));

        assertEquals(Block.WHITE_WOOL, world.overlay.get(new Vec(1, 41, 0)), "the placement landed in the overlay");
        assertTrue(instance.getBlock(1, 41, 0).isAir(), "the instance was never written");
        assertEquals(63, player.getItemInMainHand().amount(), "the held block was consumed");
    }

    @Test
    public void digFromOverlayWorld(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var world = new OverlayWorld(instance);
        world.overlay.put(new Vec(3, 41, 0), Block.STONE);

        BlockActions.dig(world, player, new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.FINISHED_DIGGING, new Vec(3, 41, 0), BlockFace.TOP, 0));

        assertEquals(Block.AIR, world.overlay.get(new Vec(3, 41, 0)), "the break landed in the overlay");
        assertTrue(instance.getBlock(3, 41, 0).isAir(), "the instance was never written");
    }
}
