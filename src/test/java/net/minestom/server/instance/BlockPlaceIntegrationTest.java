package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.BlockPlacementListener;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class BlockPlaceIntegrationTest {

    @Test
    void testPlacementOutOfLimit(Env env) {
        Instance instance = env.createFlatInstance();
        assertDoesNotThrow(() -> instance.setBlock(0, instance.getCachedDimensionType().maxY() + 1, 0, Block.STONE));
        assertDoesNotThrow(() -> instance.setBlock(0, instance.getCachedDimensionType().minY() - 1, 0, Block.STONE));
    }

    @Test
    void testPlacementOutOfBorder(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setWorldBorder(WorldBorder.DEFAULT_BORDER.withDiameter(1));
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.STONE, 5));

        // Should be air, then we place (this is outside the border)
        assertEquals(Block.AIR, instance.getBlock(3, 40, 0));
        var placePacket = new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Pos(3, 39, 0), BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, 1);
        BlockPlacementListener.listener(placePacket, player);

        // Should still be air
        var placedBlock = instance.getBlock(3, 40, 0);
        assertEquals(Block.AIR, placedBlock);
    }

    @Test
    void testPlacementAtMinus64(Env env) {
        Instance instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, -64, 0));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.STONE, 5));
        env.tick(); // World border tick to update distance

        // Should be air, then we place
        assertEquals(Block.AIR, instance.getBlock(3, -64, 0));
        var placePacket = new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Pos(3, -64, 0), BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, 1);
        BlockPlacementListener.listener(placePacket, player);

        // Should be stone.
        var placedBlock = instance.getBlock(3, -64, 0);
        assertEquals(Block.STONE, placedBlock);
    }
}
