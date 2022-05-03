package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityBoundingBoxIntegrationTest {
    @Test
    public void pose(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();

        // Bounding box should be from the registry
        assertEquals(player.getEntityType().registry().boundingBox(), player.getBoundingBox());
        player.setPose(Entity.Pose.STANDING);
        assertEquals(player.getEntityType().registry().boundingBox(), player.getBoundingBox());

        player.setPose(Entity.Pose.SLEEPING);
        assertEquals(new BoundingBox(0.2, 0.2, 0.2), player.getBoundingBox());

        player.setPose(Entity.Pose.SNEAKING);
        assertEquals(new BoundingBox(0.6, 1.5, 0.6), player.getBoundingBox());

        player.setPose(Entity.Pose.FALL_FLYING);
        assertEquals(new BoundingBox(0.6, 0.6, 0.6), player.getBoundingBox());
    }
}
