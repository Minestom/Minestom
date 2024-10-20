package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;


@ExtendWith(MicrotusExtension.class)
public class ExplosionIntegrationTest {

    // Checks that only nearby players actually receive the packet from the server
    @Test
    void sendToNearbyPlayers(Env env) {
        var instance = env.createFlatInstance();
        instance.setExplosionSupplier(new TestExplosionSupplierImpl());

        var connection1 = env.createConnection();
        var connection2 = env.createConnection();
        var connection3 = env.createConnection();

        // Assumes that the default ServerFlag.EXPLOSION_SEND_DISTANCE is set to 100 blocks
        var player1 = connection1.connect(instance, new Pos(0, 41, 0));
        var player2 = connection2.connect(instance, new Pos(50, 41, 0));
        var player3 = connection3.connect(instance, new Pos(0, 41, 110));

        var packetTracker1 = connection1.trackIncoming(ExplosionPacket.class);
        var packetTracker2 = connection2.trackIncoming(ExplosionPacket.class);
        var packetTracker3 = connection3.trackIncoming(ExplosionPacket.class);

        instance.explode(0, 41, 0, 1);
        packetTracker1.assertSingle();
        packetTracker2.assertSingle();
        packetTracker3.assertEmpty();
    }


    public class TestExplosionSupplierImpl implements ExplosionSupplier {
        @Override
        public Explosion createExplosion(float centerX, float centerY, float centerZ, float strength, CompoundBinaryTag additionalData) {
            return new TestExplosionImpl(centerX, centerY, centerZ, strength);
        }

        public class TestExplosionImpl extends Explosion {

            protected TestExplosionImpl(float centerX, float centerY, float centerZ, float strength) {
                super(centerX, centerY, centerZ, strength);
            }
            @Override
            protected List<Point> prepare(Instance instance) {
                return List.of(new Pos(getCenterX(), getCenterY(), getCenterZ()));
            }
        }
    }

}
