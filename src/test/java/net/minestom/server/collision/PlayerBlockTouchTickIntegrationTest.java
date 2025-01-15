package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.NamespaceID;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Separate because of how movement is calculated between players and entities.
// We should expect this bad behavior from the player but not entities. unless the physics engine has gets a controller input vector.
@EnvTest
public class PlayerBlockTouchTickIntegrationTest {
    @Test
    public void playerPhysicsCheckTouchTick(Env env) {
        var instance = env.createFlatInstance();

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.blockPosition()));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        var customBlock = Block.STONE.withHandler(handler);
        var blockPos = Set.of(new Vec(-1, 43, 0),
                new Vec(1, 43, 0),
                new Vec(0, 43, -1),
                new Vec(0, 43, 1),
                new Vec(0, 45, 0) // Y+
        );

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(0, 42, 0, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        var spawnPoint = new Pos(0.5, 43, 0.5);
        var player = env.createPlayer(instance, spawnPoint);

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.5)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(0, 42, 0, customBlock);
        player.tick(0);
        assertEquals(Set.of(new Vec(0, 42, 0)), positions);

        assertEquals(instance, player.getInstance());
    }
    @Test
    public void playerPhysicsCheckTouchTickFarPositiveXNegativeZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(1000, 1000, -1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.blockPosition()));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        var customBlock = Block.STONE.withHandler(handler);
        var offset = new Vec(1000, 0, -1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(1000, 42, -1000, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        var spawnPoint = new Pos(1000.5, 43, -999.5);
        var player = env.createPlayer(instance, spawnPoint);

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.5)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(1000, 42, -1000, customBlock);
        player.tick(0);
        assertEquals(Set.of(new Vec(1000, 42, -1000)), positions);

        assertEquals(instance, player.getInstance());
    }

    @Test
    public void playerPhysicsCheckTouchTickFarNegativeXPositiveZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(-1000, 1000, 1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.blockPosition()));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        var customBlock = Block.STONE.withHandler(handler);
        var offset = new Vec(-1000, 0, 1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(-1000, 42, 1000, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        var spawnPoint = new Pos(-999.5, 43, 1000.5);
        var player = env.createPlayer(instance, spawnPoint);

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.5)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(-1000, 42, 1000, customBlock);
        player.tick(0);
        assertEquals(Set.of(new Vec(-1000, 42, 1000)), positions);

        assertEquals(instance, player.getInstance());
    }

    @Test
    public void playerPhysicsCheckTouchTickFarPositiveXZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(1000, 1000, 1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.blockPosition()));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        var customBlock = Block.STONE.withHandler(handler);
        var offset = new Vec(1000, 0, 1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(1000, 42, 1000, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        var spawnPoint = new Pos(1000.5, 43, 1000.5);
        var player = env.createPlayer(instance, spawnPoint);

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.5)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(1000, 42, 1000, customBlock);
        player.tick(0);
        assertEquals(Set.of(new Vec(1000, 42, 1000)), positions);

        assertEquals(instance, player.getInstance());
    }

    @Test
    public void playerPhysicsCheckTouchTickFarNegativeXZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(-1000, 1000, -1000));

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.blockPosition()));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        var customBlock = Block.STONE.withHandler(handler);
        var offset = new Vec(-1000, 0, -1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(-1000, 42, -1000, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        var spawnPoint = new Pos(-999.5, 43, -999.5);
        var player = env.createPlayer(instance, spawnPoint);

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.5)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(-1000, 42, -1000, customBlock);
        player.tick(0);
        assertEquals(Set.of(new Vec(-1000, 42, -1000)), positions);

        assertEquals(instance, player.getInstance());
    }
    
    @Test
    public void playerTouchPhysicsTestBadBehavior(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 43, 0));

        var block = Block.STONE.withHandler(new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertEquals(new Vec(0, 42, 0), touch.blockPosition());
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        });
        instance.setBlock(0, 42, 0, block);

        player.tick(0);
    }
}
