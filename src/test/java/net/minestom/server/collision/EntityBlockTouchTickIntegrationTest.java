package net.minestom.server.collision;

import net.kyori.adventure.key.Key;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityBlockTouchTickIntegrationTest {
    @Test
    public void entityPhysicsCheckTouchTick(Env env) {
        var instance = env.createFlatInstance();

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.getBlockPosition()));
            }

            @Override
            public @NotNull Key key() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(0, 42, 0, Block.STONE.withHandler(handler));
        instance.setBlock(0, 42, 1, Block.STONE.withHandler(handler));
        instance.setBlock(0, 43, 1, Block.STONE.withHandler(handler));
        instance.setBlock(0, 43, -1, Block.STONE.withHandler(handler));
        instance.setBlock(1, 42, 1, Block.STONE.withHandler(handler));
        instance.setBlock(1, 42, 0, Block.STONE.withHandler(handler));
        instance.setBlock(0, 42, 10, Block.STONE.withHandler(handler));

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();

        entity.tick(0);

        assertEquals(Set.of(new Vec(0, 42, 0),
                new Vec(0, 42, 1),
                new Vec(0, 43, 1)),
                positions);

        assertEquals(instance, entity.getInstance());
    }

    @Test
    public void entityPhysicsCheckTouchTickFarZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(1000, 1000, 1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.getBlockPosition()));
            }

            @Override
            public @NotNull Key key() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(1000, 42, 1000, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 42, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 43, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 43, 999, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 42, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 42, 1000, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 42, 1010, Block.STONE.withHandler(handler));

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(1000, 42, 1000.7)).join();

        entity.tick(0);

        assertEquals(Set.of(
                new Vec(1000, 42, 1000),
                new Vec(1000, 42, 1001),
                new Vec(1000, 43, 1001)
            ), positions);

        assertEquals(instance, entity.getInstance());
    }

    @Test
    public void entityPhysicsCheckTouchTickFarX(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(1000, 1000, 1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.getBlockPosition()));
            }

            @Override
            public @NotNull Key key() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(1000, 42, 1000, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 42, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 43, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 43, 999, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 43, 999, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 42, 999, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 42, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 43, 1000, Block.STONE.withHandler(handler));
        instance.setBlock(999, 42, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 43, 1001, Block.STONE.withHandler(handler));
        instance.setBlock(1001, 42, 1000, Block.STONE.withHandler(handler));
        instance.setBlock(1000, 42, 1010, Block.STONE.withHandler(handler));

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(1000.699, 42, 1000)).join();

        entity.tick(0);

        assertEquals(Set.of(
                new Vec(1000, 43, 999),
                new Vec(1000, 42, 1000),
                new Vec(1001, 43, 1000),
                new Vec(1001, 42, 1000),
                new Vec(1001, 42, 999),
                new Vec(1001, 43, 999)
            ), positions);

        assertEquals(instance, entity.getInstance());
    }

    @Test
    public void entityPhysicsCheckTouchTickFarNegative(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(-1000, 44, -1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.getBlockPosition()));
            }

            @Override
            public @NotNull Key key() {
                return Key.key("minestom:test");
            }
        };

        instance.setBlock(-1000, 42, -1000, Block.STONE.withHandler(handler));
        instance.setBlock(-1000, 42, -1001, Block.STONE.withHandler(handler));
        instance.setBlock(-1000, 43, -1001, Block.STONE.withHandler(handler));
        instance.setBlock(-1000, 43, -999, Block.STONE.withHandler(handler));
        instance.setBlock(-1001, 43, -999, Block.STONE.withHandler(handler));
        instance.setBlock(-1001, 42, -999, Block.STONE.withHandler(handler));
        instance.setBlock(-1001, 42, -1001, Block.STONE.withHandler(handler));
        instance.setBlock(-1001, 43, -1000, Block.STONE.withHandler(handler));
        instance.setBlock(-999, 42, -1001, Block.STONE.withHandler(handler));
        instance.setBlock(-1001, 43, -1001, Block.STONE.withHandler(handler));
        instance.setBlock(-1001, 42, -1000, Block.STONE.withHandler(handler));
        instance.setBlock(-1000, 42, -1010, Block.STONE.withHandler(handler));

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(-1000.301, 42, -1000)).join();

        entity.tick(0);

        assertEquals(Set.of(
                new Vec(-1001, 43, -1000),
                new Vec(-1001, 42, -1000),
                new Vec(-1001, 43, -1001),
                new Vec(-1001, 42, -1001),
                new Vec(-1000, 43, -1001),
                new Vec(-1000, 42, -1001),
                new Vec(-1000, 42, -1000)
        ), positions);

        assertEquals(instance, entity.getInstance());
    }
}
