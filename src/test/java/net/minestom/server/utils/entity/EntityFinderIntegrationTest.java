package net.minestom.server.utils.entity;

import net.minestom.server.command.ServerSender;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Range;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityFinderIntegrationTest {
    private static final Pos ORIGIN = new Pos(0, 40, 0);

    private static Entity spawn(Instance instance, EntityType type, double x) {
        var entity = new Entity(type);
        entity.setInstance(instance, ORIGIN.add(x, 0, 0)).join();
        return entity;
    }

    private static Entity spawnZombie(Instance instance, double x) {
        return spawn(instance, EntityType.ZOMBIE, x);
    }

    private static EntityFinder createFinder(EntityFinder.TargetSelector selector) {
        return new EntityFinder()
                .setTargetSelector(selector)
                .setStartPosition(ORIGIN);
    }

    @Test
    public void nearestPlayerAppliesFiltersBeforeSelection(Env env) {
        var instance = env.createFlatInstance();
        var nearSurvival = env.createPlayer(instance, ORIGIN.add(1, 0, 0));
        var farCreative = env.createPlayer(instance, ORIGIN.add(10, 0, 0));
        nearSurvival.setGameMode(GameMode.SURVIVAL);
        farCreative.setGameMode(GameMode.CREATIVE);

        var finder = createFinder(EntityFinder.TargetSelector.NEAREST_PLAYER)
                .setGameMode(GameMode.CREATIVE, EntityFinder.ToggleableType.INCLUDE);

        assertEquals(List.of(farCreative), finder.find(instance, null));
    }

    @Test
    public void randomPlayerAppliesFiltersBeforeSelection(Env env) {
        var instance = env.createFlatInstance();
        var creative = env.createPlayer(instance, ORIGIN);
        creative.setGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 3; i++) {
            env.createPlayer(instance, ORIGIN).setGameMode(GameMode.SURVIVAL);
        }

        var finder = new EntityFinder()
                .setTargetSelector(EntityFinder.TargetSelector.RANDOM_PLAYER)
                .setGameMode(GameMode.CREATIVE, EntityFinder.ToggleableType.INCLUDE);

        assertTrue(finder.find(instance, null).isEmpty());
        creative.setGameMode(GameMode.CREATIVE);

        for (int attempt = 0; attempt < 20; attempt++) {
            assertEquals(List.of(creative), finder.find(instance, null));
        }
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
        var near = spawnZombie(instance, 1);
        var far = spawnZombie(instance, 50);

        var finder = createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setDistance(new Range.Double(5, Float.MAX_VALUE));

        assertEquals(List.of(far), finder.find(instance, null));
    }

    @Test
    public void openLowerBoundDistanceRange(Env env) {
        var instance = env.createFlatInstance();
        var near = spawnZombie(instance, 1);
        var far = spawnZombie(instance, 50);

        var finder = new ArgumentEntity("selector").parse(new ServerSender(), "@e[distance=..1.5]");
        finder.setStartPosition(ORIGIN);

        assertEquals(List.of(near), finder.find(instance, null));
    }

    @Test
    public void negativeMinDistanceClamped(Env env) {
        var instance = env.createFlatInstance();
        var near = spawnZombie(instance, 1);
        var far = spawnZombie(instance, 50);

        var finder = createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setDistance(new Range.Double(-10, 5));

        assertEquals(List.of(near), finder.find(instance, null));
    }

    @Test
    public void sortNearestAndFurthest(Env env) {
        var instance = env.createFlatInstance();
        var second = spawnZombie(instance, 2);
        var third = spawnZombie(instance, 3);
        var first = spawnZombie(instance, 1);

        assertEquals(List.of(first, second, third),
                createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                        .setEntitySort(EntityFinder.EntitySort.NEAREST)
                        .find(instance, null));

        assertEquals(List.of(third, second, first),
                createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                        .setEntitySort(EntityFinder.EntitySort.FURTHEST)
                        .find(instance, null));
    }

    @Test
    public void randomSortWithLimit(Env env) {
        var instance = env.createFlatInstance();
        for (int i = 0; i < 3; i++) {
            spawnZombie(instance, i);
        }

        var finder = createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setEntitySort(EntityFinder.EntitySort.RANDOM)
                .setLimit(2);

        assertEquals(2, finder.find(instance, null).size());
    }

    @Test
    public void nearestPlayerSortOverride(Env env) {
        var instance = env.createFlatInstance();
        var near = env.createPlayer(instance, ORIGIN.add(1, 0, 0));
        var far = env.createPlayer(instance, ORIGIN.add(10, 0, 0));

        var finder = createFinder(EntityFinder.TargetSelector.NEAREST_PLAYER)
                .setEntitySort(EntityFinder.EntitySort.FURTHEST);

        assertEquals(List.of(far), finder.find(instance, null));
    }

    @Test
    public void nearestEntityReturnsSingleNearest(Env env) {
        var instance = env.createFlatInstance();
        var middle = spawnZombie(instance, 5);
        var far = spawnZombie(instance, 10);
        var near = spawnZombie(instance, 1);

        var finder = createFinder(EntityFinder.TargetSelector.NEAREST_ENTITY);
        assertEquals(List.of(near), finder.find(instance, null));
    }

    @Test
    public void nearestEntityAppliesFiltersBeforeSelection(Env env) {
        var instance = env.createFlatInstance();
        var nearSheep = spawn(instance, EntityType.SHEEP, 1);
        var farPig = spawn(instance, EntityType.PIG, 10);

        var finder = createFinder(EntityFinder.TargetSelector.NEAREST_ENTITY)
                .setEntity(EntityType.PIG, EntityFinder.ToggleableType.INCLUDE);

        assertEquals(List.of(farPig), finder.find(instance, null));
    }

    @Test
    public void nearestEntitySortOverride(Env env) {
        var instance = env.createFlatInstance();
        var near = spawnZombie(instance, 1);
        var far = spawnZombie(instance, 10);

        var finder = createFinder(EntityFinder.TargetSelector.NEAREST_ENTITY)
                .setEntitySort(EntityFinder.EntitySort.FURTHEST);

        assertEquals(List.of(far), finder.find(instance, null));
    }

    @Test
    public void sortNearestWithLimit(Env env) {
        var instance = env.createFlatInstance();
        var third = spawnZombie(instance, 3);
        var first = spawnZombie(instance, 1);
        var fourth = spawnZombie(instance, 4);
        var second = spawnZombie(instance, 2);

        var finder = createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setEntitySort(EntityFinder.EntitySort.NEAREST)
                .setLimit(2);

        assertEquals(List.of(first, second), finder.find(instance, null));
    }

    @Test
    public void sortFurthestWithLimit(Env env) {
        var instance = env.createFlatInstance();
        var third = spawnZombie(instance, 3);
        var first = spawnZombie(instance, 1);
        var fourth = spawnZombie(instance, 4);
        var second = spawnZombie(instance, 2);

        var finder = createFinder(EntityFinder.TargetSelector.ALL_ENTITIES)
                .setEntitySort(EntityFinder.EntitySort.FURTHEST)
                .setLimit(2);

        assertEquals(List.of(fourth, third), finder.find(instance, null));
    }
}
