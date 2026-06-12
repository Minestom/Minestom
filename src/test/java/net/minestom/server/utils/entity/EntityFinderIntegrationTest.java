package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.utils.Range;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityFinderIntegrationTest {
    private static final Pos ORIGIN = new Pos(0, 40, 0);

    @Test
    public void nearestPlayerAppliesFiltersBeforeSelection(Env env) {
        var instance = env.createFlatInstance();

        var nearSurvival = env.createPlayer(instance, ORIGIN.add(1, 0, 0));
        var farCreative = env.createPlayer(instance, ORIGIN.add(10, 0, 0));

        nearSurvival.setGameMode(GameMode.SURVIVAL);
        farCreative.setGameMode(GameMode.CREATIVE);

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.NEAREST_PLAYER)
                .setStartPosition(ORIGIN)
                .setGameMode(GameMode.CREATIVE, EntityFinder.ToggleableType.INCLUDE);

        assertEquals(List.of(farCreative), finder.find(instance, null));
    }

    @Test
    public void randomPlayerWithoutMatchReturnsEmpty(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, ORIGIN);
        player.setGameMode(GameMode.SURVIVAL);

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.RANDOM_PLAYER)
                .setGameMode(GameMode.CREATIVE, EntityFinder.ToggleableType.INCLUDE);

        assertTrue(finder.find(instance, null).isEmpty());
    }

    @Test
    public void randomPlayerWithoutAnyPlayersReturnsEmpty(Env env) {
        var instance = env.createFlatInstance();

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.RANDOM_PLAYER);

        assertTrue(finder.find(instance, null).isEmpty());
    }

    @Test
    public void openEndedDistanceRange(Env env) {
        var instance = env.createFlatInstance();

        var near = new Entity(EntityType.ZOMBIE);
        var far = new Entity(EntityType.ZOMBIE);

        near.setInstance(instance, ORIGIN.add(1, 0, 0)).join();
        far.setInstance(instance, ORIGIN.add(50, 0, 0)).join();

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(ORIGIN)
                .setDistance(new Range.Double(5, Float.MAX_VALUE));

        assertEquals(List.of(far), finder.find(instance, null));
    }

    @Test
    public void openLowerBoundDistanceRange(Env env) {
        var instance = env.createFlatInstance();

        var near = new Entity(EntityType.ZOMBIE);
        var far = new Entity(EntityType.ZOMBIE);

        near.setInstance(instance, ORIGIN.add(1, 0, 0)).join();
        far.setInstance(instance, ORIGIN.add(50, 0, 0)).join();

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(ORIGIN)
                .setDistance(new Range.Double(-Float.MAX_VALUE, 5));

        assertEquals(List.of(near), finder.find(instance, null));
    }

    @Test
    public void sortNearestAndFurthest(Env env) {
        var instance = env.createFlatInstance();

        var first = new Entity(EntityType.ZOMBIE);
        var second = new Entity(EntityType.ZOMBIE);
        var third = new Entity(EntityType.ZOMBIE);

        second.setInstance(instance, ORIGIN.add(2, 0, 0)).join();
        third.setInstance(instance, ORIGIN.add(3, 0, 0)).join();
        first.setInstance(instance, ORIGIN.add(1, 0, 0)).join();

        var nearestFinder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(ORIGIN)
                .setEntitySort(EntityFinder.EntitySort.NEAREST);

        assertEquals(List.of(first, second, third), nearestFinder.find(instance, null));

        var furthestFinder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(ORIGIN)
                .setEntitySort(EntityFinder.EntitySort.FURTHEST);

        assertEquals(List.of(third, second, first), furthestFinder.find(instance, null));
    }

    @Test
    public void randomSortWithLimit(Env env) {
        var instance = env.createFlatInstance();

        for (int i = 0; i < 3; i++) {
            new Entity(EntityType.ZOMBIE).setInstance(instance, ORIGIN.add(i, 0, 0)).join();
        }

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setStartPosition(ORIGIN)
                .setEntitySort(EntityFinder.EntitySort.RANDOM)
                .setLimit(2);

        assertEquals(2, finder.find(instance, null).size());
    }

    @Test
    public void nearestPlayerSortOverride(Env env) {
        var instance = env.createFlatInstance();
        var near = env.createPlayer(instance, ORIGIN.add(1, 0, 0));
        var far = env.createPlayer(instance, ORIGIN.add(10, 0, 0));

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.NEAREST_PLAYER)
                .setStartPosition(ORIGIN)
                .setEntitySort(EntityFinder.EntitySort.FURTHEST);

        assertEquals(List.of(far), finder.find(instance, null));
    }
}
