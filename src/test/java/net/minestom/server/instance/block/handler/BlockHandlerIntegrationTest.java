package net.minestom.server.instance.block.handler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
class BlockHandlerIntegrationTest {

    @Test
    void testOnPlace(Env env) {
        var instance = env.createFlatInstance();
        var blockPosition = new Vec(-64, 40, 64);

        var handler = new BlockHandler() {
            @Override
            public void onPlace(@NotNull Placement placement) {
                assertEquals(blockPosition, placement.getBlockPosition());
            }

            @Override
            public @NotNull Key getKey() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(blockPosition, Block.STONE.withHandler(handler));
    }

    @Test
    void testOnDestroy(Env env) {
        var instance = env.createFlatInstance();
        var blockPosition = new Vec(64, 40, -64);

        var handler = new BlockHandler() {
            @Override
            public void onDestroy(@NotNull Destroy destroy) {
                assertEquals(blockPosition, destroy.getBlockPosition());
            }

            @Override
            public @NotNull Key getKey() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(blockPosition, Block.STONE.withHandler(handler));
        instance.setBlock(blockPosition, Block.AIR);
    }

    @Test
    void testOnInteract(Env env) {
        var instance = env.createFlatInstance();
        var blockPosition = new Vec(-64, 40, 64);

        AtomicBoolean interacted = new AtomicBoolean(false);
        var handler = new BlockHandler() {
            @Override
            public boolean onInteract(@NotNull Interaction interaction) {
                interacted.set(true);
                assertEquals(blockPosition, interaction.getBlockPosition());
                return false;
            }

            @Override
            public @NotNull Key getKey() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(blockPosition, Block.STONE.withHandler(handler));
        var player = env.createPlayer(instance, blockPosition.asPosition());
        player.addPacketToQueue(new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, blockPosition, BlockFace.TOP, 0, 0, 0, false, false, 1));
        player.interpretPacketQueue(); // Use packets

        assertTrue(interacted.get());
    }

    @Test
    void testTick(Env env) {
        var instance = env.createFlatInstance();
        var blockPosition = new BlockVec(64, 40, -64);

        AtomicBoolean ticked = new AtomicBoolean(false);
        var handler = new BlockHandler() {
            @Override
            public void tick(@NotNull Tick tick) {
                ticked.set(true);
                assertEquals(tick.getBlockPosition(), blockPosition.asVec());
            }

            @Override
            public @NotNull Key getKey() {
                return Key.key("minestom:test");
            }

            @Override
            public boolean isTickable() {
                return true;
            }
        };

        instance.setBlock(blockPosition, Block.STONE.withHandler(handler));
        // Tick the chunk
        var chunk = instance.getChunk(4, -4);
        assertNotNull(chunk);
        chunk.tick(0);

        assertTrue(ticked.get());
    }

    @Test
    void testTickRemoved(Env env) {
        var instance = env.createFlatInstance();
        var blockPosition = new BlockVec(64, 40, -64);

        AtomicBoolean ticked = new AtomicBoolean(false);
        var handler = new BlockHandler() {
            @Override
            public void tick(@NotNull Tick tick) {
                ticked.set(true);
                assertEquals(tick.getBlockPosition(), blockPosition.asVec());
            }

            @Override
            public @NotNull Key getKey() {
                return Key.key("minestom:test");
            }

            @Override
            public boolean isTickable() {
                return true;
            }
        };

        instance.setBlock(blockPosition, Block.STONE.withHandler(handler));
        // Tick the chunk
        var chunk = instance.getChunk(4, -4);
        assertNotNull(chunk);
        chunk.tick(0);

        assertTrue(ticked.get());
        // Now assume there is no chunk left.
        ticked.set(false);
        instance.setBlock(blockPosition, Block.AIR);
        chunk.tick(0);
        assertFalse(ticked.get(), "Chunk ticked block when it no longer exists!");
    }
}
