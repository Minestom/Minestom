package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityFinderIntegrationTest {

    /**
     * Stores values related to an ongoing test
     * @param instance The instance
     * @param entities The created entities (in creation order, which matches ascending distance from origin)
     */
    private record TestContext(Instance instance, List<Entity> entities) {}

    /**
     * Sets up a test instance with a player at origin and 9 surrounding zombies
     * @param env The environment
     * @return The test context
     */
    private TestContext setupTest(Env env) {
        Instance instance = env.createFlatInstance();

        List<Entity> entities = new ArrayList<>();
        entities.add(env.createPlayer(instance, new Pos(0, 41, 0)));

        int angle = 0;
        for (int i = 1; i < 10; i++) {
            Entity entity = new Entity(EntityType.ZOMBIE);
            entity.setHasPhysics(false);
            entity.setInstance(instance, new Pos(
                    Math.cos(angle) * i,
                    41,
                    Math.sin(angle) * i
            )).join();

            entities.add(entity);
            angle += 45;
        }

        return new TestContext(instance, entities);
    }

    @Test
    public void findNearestPlayer(Env env) {
        TestContext ctx = setupTest(env);

        EntityFinder finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.NEAREST_PLAYER)
                .setStartPosition(new Pos(10, 41, 10));
        List<Entity> results = finder.find(ctx.instance, null);
        assertEquals(1, results.size(), "TargetSelector.NEAREST_PLAYER result should only contain the player");
        assertEquals(ctx.entities.getFirst(), results.getFirst(), "TargetSelector.NEAREST_PLAYER resulting entity should be the player");
    }

    @Test
    public void sortNearest(Env env) {
        TestContext ctx = setupTest(env);

        EntityFinder finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(new Pos(0, 41, 0))
                .setEntitySort(EntityFinder.EntitySort.NEAREST);
        List<Entity> results = finder.find(ctx.instance, null);
        assertEquals(ctx.entities, results, "EntitySort.NEAREST result should be sorted from nearest to furthest");
    }

    @Test
    public void sortFurthest(Env env) {
        TestContext ctx = setupTest(env);

        EntityFinder finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(new Pos(0, 41, 0))
                .setEntitySort(EntityFinder.EntitySort.FURTHEST);
        List<Entity> results = finder.find(ctx.instance, null);
        assertEquals(ctx.entities.reversed(), results, "EntitySort.NEAREST result should be sorted from nearest to furthest");
    }

    @Test
    public void sortNearestWithLimit(Env env) {
        TestContext ctx = setupTest(env);

        EntityFinder finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(new Pos(0, 41, 0))
                .setEntitySort(EntityFinder.EntitySort.NEAREST)
                .setLimit(4);
        List<Entity> results = finder.find(ctx.instance, null);
        assertEquals(ctx.entities.subList(0, 4), results, "EntitySort.NEAREST result should be sorted from nearest to furthest");
    }

}
