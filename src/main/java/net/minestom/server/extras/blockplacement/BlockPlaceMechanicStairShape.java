package net.minestom.server.extras.blockplacement;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

class BlockPlaceMechanicStairShape {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(block, event.getInstance(), event.getBlockPosition()));
    }

    static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(block, event.getInstance(), event.getBlockPosition()));
    }

    private static Block update(Block block, Instance instance, Point position) {
        String facingStr = block.getProperty("facing");
        Facing facing = getFacing(facingStr);
        Shape shape = getShape(instance, position, facing);

        return block.withProperty("shape", shape.toString().toLowerCase());
    }

    private enum Shape {
        STRAIGHT,
        OUTER_LEFT,
        OUTER_RIGHT,
        INNER_LEFT,
        INNER_RIGHT
    }

    private static Facing getFacing(String facing) {
        switch(facing.toLowerCase()) {
            case "east": return Facing.EAST;
            case "west": return Facing.WEST;
            case "south": return Facing.SOUTH;
            default: return Facing.NORTH;
        }
    }

    private enum Facing {
        NORTH(new Vec(0, 0, 1), new Vec(0, 0, -1)),
        EAST(new Vec(-1, 0, 0), new Vec(1, 0, 0)),
        SOUTH(new Vec(0, 0, -1), new Vec(0, 0, 1)),
        WEST(new Vec(1, 0, 0), new Vec(-1, 0, 0));

        private final Point front;
        private final Point back;

        Facing(@NotNull Point front, @NotNull Point back) {
            this.front = front;
            this.back = back;
        }

        @NotNull
        public Pair<@Nullable Shape, @Nullable Facing> getFront(@NotNull Instance instance, @NotNull Point point) {
            return this.getProperties(instance, point.add(this.front));
        }

        @NotNull
        public Pair<@Nullable Shape, @Nullable Facing> getBack(@NotNull Instance instance, @NotNull Point point) {
            return this.getProperties(instance, point.add(this.back));
        }

        @NotNull
        private Pair<@Nullable Shape, @Nullable Facing> getProperties(@NotNull Instance instance, @NotNull Point point) {
            Block block = instance.getBlock(point);
            if (block.isAir()) {
                return Pair.of(null, null);
            }
            try {
                Shape shape = Shape.valueOf(block.getProperty("shape").toUpperCase(Locale.ROOT));
                Facing facing = Facing.valueOf(block.getProperty("facing").toUpperCase(Locale.ROOT));
                return Pair.of(shape, facing);
            } catch (Exception ex) {
                return Pair.of(null, null);
            }
        }
    }

    @NotNull
    private static Shape getShape(@NotNull Instance instance, @NotNull Point point, @NotNull Facing facing) {
        Pair<Shape, Facing> front = facing.getFront(instance, point);
        Pair<Shape, Facing> back = facing.getBack(instance, point);

        Shape shape = getShapeFromSide(front, facing, Shape.INNER_RIGHT, Shape.INNER_LEFT);
        if (shape == null) {
            shape = getShapeFromSide(back, facing, Shape.OUTER_RIGHT, Shape.OUTER_LEFT);
        }
        return shape == null ? Shape.STRAIGHT : shape;
    }

    @Nullable
    private static Shape getShapeFromSide(@NotNull Pair<Shape, Facing> side, @NotNull Facing facing, @NotNull Shape right, @NotNull Shape left) {
        if (side.left() == null) {
            return null;
        }
        Facing sideFacing = side.right();
        if (facing.equals(Facing.NORTH)) {
            if (sideFacing.equals(Facing.EAST)) {
                return right;
            } else if (sideFacing.equals(Facing.WEST)) {
                return left;
            }
        } else if (facing.equals(Facing.SOUTH)) {
            if (sideFacing.equals(Facing.EAST)) {
                return left;
            } else if (sideFacing.equals(Facing.WEST)) {
                return right;
            }
        } else if (facing.equals(Facing.EAST)) {
            if (sideFacing.equals(Facing.SOUTH)) {
                return right;
            } else if (sideFacing.equals(Facing.NORTH)) {
                return left;
            }
        } else if (facing.equals(Facing.WEST)) {
            if (sideFacing.equals(Facing.SOUTH)) {
                return left;
            } else if (sideFacing.equals(Facing.NORTH)) {
                return right;
            }
        }
        return null;
    }

}
