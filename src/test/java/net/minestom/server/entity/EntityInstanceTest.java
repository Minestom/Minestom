package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvParameterResolver;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.JoinGamePacket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EnvParameterResolver.class)
public class EntityInstanceTest {

    @Test
    public void entityJoin(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());
    }

    @Test
    public void playerJoin(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());
    }

    @Test
    public void playerJoinPacket(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var tracker = connection.trackIncoming(JoinGamePacket.class);
        var tracker2 = connection.trackIncoming(ServerPacket.class);
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        assertEquals(1, tracker.collect().size());
        assertTrue(tracker2.collect().size() > 1);
    }
}
