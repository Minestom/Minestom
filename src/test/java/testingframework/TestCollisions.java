package testingframework;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.testing.MinestomTest;
import net.minestom.testing.TestEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCollisions {

    @MinestomTest
    public void handleCorners(TestEnvironment env) {
        Entity entity = new Entity(EntityType.ARMOR_STAND);
        entity.getPosition().setX(0.5);
        entity.getPosition().setY(1);
        entity.getPosition().setZ(0.5);
        entity.setNoGravity(true);

        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.loadChunk(0, 0);
        // corner
        instance.setBlock(0, 1, 1, Block.BEDROCK);

        /* Starting position (A: air, E: entity, B: bedrock)

        Z
        ^
        |
        |
        +---> X

            BA
            EA

            Expected ending position: (slide against wall)
            BA
            AE

            What should not happen: (skip corner)
            BE
            AA
        */

        entity.setInstance(instance);

        Position outPosition = new Position();
        Vector outVelocity = new Vector();
        CollisionUtils.handlePhysics(entity, new Vector(1, 0.5, 1), outPosition, outVelocity);

        assertEquals(1.5, outPosition.getX(), 0.15);
        //assertEquals(0.5, outPosition.getY(), 0.15);
        assertEquals(0.5, outPosition.getZ(), 0.15);
    }
}
