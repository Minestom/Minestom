package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.SuspiciousGravelBlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class BlockPlacementRuleIntegrationTest {

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
