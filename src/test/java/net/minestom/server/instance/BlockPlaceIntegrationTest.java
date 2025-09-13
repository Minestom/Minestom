package net.minestom.server.instance;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.ItemBlockState;
import net.minestom.server.listener.BlockPlacementListener;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        var placePacket = new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Pos(3, 39, 0), BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, false, 1);
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
        var placePacket = new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Pos(3, -64, 0), BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, false, 1);
        BlockPlacementListener.listener(placePacket, player);

        // Should be stone.
        var placedBlock = instance.getBlock(3, -64, 0);
        assertEquals(Block.STONE, placedBlock);
    }

    @Test
    void testPlaceNoUpdateWithItemBlockStateComponent(Env env) {
        Instance instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, -64, 0));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.STONE_STAIRS, 5)
                .with(DataComponents.BLOCK_STATE, new ItemBlockState("facing", "west")));

        var placeCollector = env.trackEvent(PlayerBlockPlaceEvent.class, EventFilter.PLAYER, player);

        var placePacket = new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Pos(3, -64, 0), BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, false, 1);
        BlockPlacementListener.listener(placePacket, player);

        // Should default to no updates because of the BLOCK_STATE component
        placeCollector.assertSingle(event -> assertFalse(event.shouldDoBlockUpdates()));
    }

    @Test
    void testPlaceNoUpdateBlockStateComponentBeeHiveRegression(Env env) {
        // We originally compared to an empty block state but some blocks (like bee hive)
        // have a default value, so we only should trigger no updates if the block state is
        // different from the default.
        Instance instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, -64, 0));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.BEEHIVE, 5));

        var placeCollector = env.trackEvent(PlayerBlockPlaceEvent.class, EventFilter.PLAYER, player);

        var placePacket = new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Pos(3, -64, 0), BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, false, 1);
        BlockPlacementListener.listener(placePacket, player);

        // Should have updates because we only have the default BLOCK_STATE value
        placeCollector.assertSingle(event -> assertTrue(event.shouldDoBlockUpdates()));
    }

}
