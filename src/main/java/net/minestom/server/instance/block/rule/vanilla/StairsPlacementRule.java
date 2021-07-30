package net.minestom.server.instance.block.rule.vanilla;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StairsPlacementRule extends BlockPlacementRule {

    public StairsPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        return block;
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace,
                            @NotNull Point blockPosition, @NotNull Player player) {
        Facing facing = this.getFacing(player);
        Shape shape = this.getShape(instance, blockPosition, facing);
        BlockFace half = BlockFace.BOTTOM; // waiting for new block faces to be implemented
        String waterlogged = "false"; // waiting for water to be implemented

        return block.withProperties(Map.of(
                "facing", facing.toString(),
                "half", half.toString(),
                "shape", shape.toString(),
                "waterlogged", waterlogged));

    }

    private enum Shape {
        STRAIGHT,
        OUTER_LEFT,
        OUTER_RIGHT,
        INNER_LEFT,
        INNER_RIGHT
    }

    private enum Facing {
        NORTH(
                new Vec(0, 0, 1),
                new Vec(0, 0, -1)
        ),
        EAST(
                new Vec(-1, 0, 0),
                new Vec(1, 0, 0)
        ),
        SOUTH(
                new Vec(0, 0, -1),
                new Vec(0, 0, 1)
        ),
        WEST(
                new Vec(1, 0, 0),
                new Vec(-1, 0, 0)
        );

        private final Point front;
        private final Point back;

        Facing(@NotNull Point front, @NotNull Point back) {
            this.front = front;
            this.back = back;
        }

        @NotNull
        public Pair<@Nullable Shape, @Nullable Facing> getFront(@NotNull Instance instance, @NotNull Point blockPosition) {
            // TODO FIX
            return null;
            //return this.getProperties(instance, blockPosition.clone().add(this.front));
        }

        @NotNull
        public Pair<@Nullable Shape, @Nullable Facing> getBack(@NotNull Instance instance, @NotNull Point blockPosition) {
            return this.getProperties(instance, blockPosition.add(this.back));
        }

        @NotNull
        private Pair<@Nullable Shape, @Nullable Facing> getProperties(@NotNull Instance instance, @NotNull Point blockPosition) {
            Block block = instance.getBlock(blockPosition);
            if (block.isAir()) {
                return Pair.of(null, null);
            }
            Block state = instance.getBlock(blockPosition);
            try {
                // TODO: Get properties from state
//                Shape shape = Shape.valueOf(state.getProperty("shape").toUpperCase());
//                Facing facing = Facing.valueOf(state.getProperty("facing").toUpperCase());
//                return Pair.of(shape, facing);
                return Pair.of(null, null);
            } catch (Exception ex) {
                return Pair.of(null, null);
            }
        }
    }

    @NotNull
    private Shape getShape(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Facing facing) {
        // TODO FIX
        return null;
        /*Pair<Shape, Facing> front = facing.getFront(instance, blockPosition);
        Pair<Shape, Facing> back = facing.getBack(instance, blockPosition);
        Shape shape = this.getShapeFromSide(front, facing, Shape.INNER_RIGHT, Shape.INNER_LEFT);
        if (shape == null) {
            shape = this.getShapeFromSide(back, facing, Shape.OUTER_RIGHT, Shape.OUTER_LEFT);
        }
        return shape == null ? Shape.STRAIGHT : shape;*/
    }

    @Nullable
    private Shape getShapeFromSide(@NotNull Pair<Shape, Facing> side, @NotNull Facing facing, @NotNull Shape right, @NotNull Shape left) {
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

    @NotNull
    private Facing getFacing(@NotNull Player player) {
        float degrees = (player.getPosition().yaw() - 90) % 360;
        if (degrees < 0) {
            degrees += 360;
        }
        if (0 <= degrees && degrees < 45) {
            return Facing.WEST;
        } else if (45 <= degrees && degrees < 135) {
            return Facing.NORTH;
        } else if (135 <= degrees && degrees < 225) {
            return Facing.EAST;
        } else if (225 <= degrees && degrees < 315) {
            return Facing.SOUTH;
        } else { // 315 <= degrees && degrees < 360
            return Facing.WEST;
        }
    }
}
