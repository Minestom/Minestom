package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityPhysicsIntegrationTest
{
    @Test
    public void onGround(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 40, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(1, 41, 1)).join();
        env.tick();

        // Entity shouldn't be on ground because it intitially spawns in with onGround = false
        // and a velocity of 0, it'll take 1 entity tick for gravity to be applied to their velocity
        // and a downward block collision to occur
        assertFalse(entity.onGround);
        for (int i = 0; i < 10; i++) {
            env.tick();
            assertTrue(entity.onGround, "entity needs to be grounded on tick: " + entity.getAliveTicks());
        }
    }

    @Test
    public void onGroundWithoutPhysics(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 40, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setHasPhysics(false);
        entity.setInstance(instance, new Pos(1, 41, 1)).join();

        for (int i = 0; i < 10; i++) {
            env.tick();
            assertFalse(entity.onGround, "entity shouldn't be grounded on tick: " + entity.getAliveTicks() + " due to lack of physics");
        }

        entity.setHasPhysics(true);
        for (int i = 0; i < 10; i++) {
            env.tick();
            assertTrue(entity.onGround, "entity should be grounded on tick: " + entity.getAliveTicks());
        }
    }
}
