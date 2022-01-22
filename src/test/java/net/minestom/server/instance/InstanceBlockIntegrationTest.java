package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class InstanceBlockIntegrationTest {

    @Test
    public void playerUpdate(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0)).join();

        var tracker = connection.trackIncoming();
        // Replace air
        tracker = connection.trackIncoming();
        instance.setBlock(5, 41, 0, Block.STONE);
        tracker.assertSingle(BlockChangePacket.class, packet -> {
            assertEquals(new Vec(5, 41, 0), packet.blockPosition());
            assertEquals(Block.STONE.stateId(), packet.blockStateId());
        });
    }
}
