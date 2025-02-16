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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is really a test between fast and slow touch methods.
 * As player cannot touch "quickly"
 * <p>
 * There is a bad behavior between the two, and it has to do with how entities are in blocks.
 * We should expect this bad behavior from the player but not entities.
 * Unless the physics engine gets updated to include a controller input vector.
 **/
@EnvTest
class PlayerBlockTouchTickIntegrationTest {
    @Test
    void playerPhysicsCheckTouchTick(Env env) {
        var instance = env.createFlatInstance();

        var blockPos = Set.of(new Vec(-1, 43, 0),
                new Vec(1, 43, 0),
                new Vec(0, 43, -1),
                new Vec(0, 43, 1),
                new Vec(0, 45, 0) // Y+
        );
        var blockBelow = new Vec(0, 42, 0); // Y-
        var spawnPoint = new Pos(0.5, 43, 0.5);
        var player = env.createPlayer(instance, spawnPoint);

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

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(blockBelow, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.25)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(blockBelow, customBlock);
        player.tick(0);
        assertEquals(Set.of(blockBelow), positions);
    }
    @Test
    void playerPhysicsCheckTouchTickFarPositiveXNegativeZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(1000, 1000, -1000));

        var offset = new Vec(1000, 0, -1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );
        var blockBelow = offset.add(0, 42, 0); // Y-
        var spawnPoint = offset.add(0.5, 43, 0.5).asPosition();
        var player = env.createPlayer(instance, spawnPoint);

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

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(blockBelow, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.25)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(blockBelow, customBlock);
        player.tick(0);
        assertEquals(Set.of(blockBelow), positions);
    }

    @Test
    void playerPhysicsCheckTouchTickFarNegativeXPositiveZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(-1000, 1000, 1000));

        var offset = new Vec(-1000, 0, 1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );
        var blockBelow = offset.add(0, 42, 0); // Y-
        var spawnPoint = offset.add(0.5, 43, 0.5).asPosition();
        var player = env.createPlayer(instance, spawnPoint);

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

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(blockBelow, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.25)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(blockBelow, customBlock);
        player.tick(0);
        assertEquals(Set.of(blockBelow), positions);
    }

    @Test
    void playerPhysicsCheckTouchTickFarPositiveXZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(1000, 1000, 1000));

        var offset = new Vec(1000, 0, 1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );
        var blockBelow = offset.add(0, 42, 0); // Y-
        var spawnPoint = offset.add(0.5, 43, 0.5).asPosition();
        var player = env.createPlayer(instance, spawnPoint);

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

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(blockBelow, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.25)));
                    player.tick(0);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(blockBelow, customBlock);
        player.teleport(spawnPoint);
        player.tick(0);
        assertEquals(Set.of(blockBelow), positions);
    }

    @Test
    void playerPhysicsCheckTouchTickFarNegativeXZ(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Pos(-1000, 1000, -1000));

        var offset = new Vec(-1000, 0, -1000);
        var blockPos = Set.of(offset.add(-1, 43, 0),
                offset.add(1, 43, 0),
                offset.add(0, 43, -1),
                offset.add(0, 43, 1),
                offset.add(0, 45, 0) // Y+
        );
        var blockBelow = offset.add(0, 42, 0); // Y-
        var spawnPoint = offset.add(0.5, 43, 0.5).asPosition();
        var player = env.createPlayer(instance, spawnPoint);

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

        for (var pos : blockPos) {
            instance.setBlock(pos, customBlock);
        }
        instance.setBlock(blockBelow, Block.STONE); // Regular floor as we are going to be sliding into the blocks.

        Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.DOWN)
                .map(Direction::vec)
                .forEachOrdered(vec -> {
                    player.teleport(spawnPoint.add(vec.mul(0.25)));
                    player.tick(0);
                    player.teleport(spawnPoint);
                });

        assertEquals(blockPos, positions);

        positions.clear(); // Final -Y check
        instance.setBlock(blockBelow, customBlock);
        player.tick(0);
        assertEquals(Set.of(blockBelow), positions);
    }

    @Test
    void playerTouchPhysicsTestBadBehavior(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 43, 0));

        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertEquals(new Vec(0, 42, 0), touch.blockPosition());
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };
        instance.setBlock(0, 42, 0, Block.STONE.withHandler(handler));

        player.tick(0);
    }
}
