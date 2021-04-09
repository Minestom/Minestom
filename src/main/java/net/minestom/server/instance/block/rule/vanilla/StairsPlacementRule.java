package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StairsPlacementRule extends BlockPlacementRule {

    public StairsPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public short blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, short currentStateID) {
        return currentStateID;
    }

    @Override
    public short blockPlace(@NotNull Instance instance, @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition, @NotNull Player player) {
        Facing facing = this.getFacing(player);
        Shape shape = this.getShape(instance, blockPosition, facing);
        BlockFace half = BlockFace.BOTTOM; // waiting for new block faces to be implemented
        boolean waterlogged = false; // waiting for water to be implemented
        return block.withProperties(
                "facing=" + facing.toString().toLowerCase(),
                "half=" + half.toString().toLowerCase(),
                "shape=" + shape.toString().toLowerCase(),
                "waterlogged=" + waterlogged
        );
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
                new BlockPosition(0, 0, 1),
                new BlockPosition(0, 0, -1)
        ),
        EAST(
                new BlockPosition(-1, 0, 0),
                new BlockPosition(1, 0, 0)
        ),
        SOUTH(
                new BlockPosition(0, 0, -1),
                new BlockPosition(0, 0, 1)
        ),
        WEST(
                new BlockPosition(1, 0, 0),
                new BlockPosition(-1, 0, 0)
        );

        private final BlockPosition front;
        private final BlockPosition back;

        Facing(@NotNull BlockPosition front, @NotNull BlockPosition back) {
            this.front = front;
            this.back = back;
        }

        @NotNull
        public Pair<@Nullable Shape, @Nullable Facing> getFront(@NotNull Instance instance, @NotNull BlockPosition blockPosition) {
            return this.getProperties(instance, blockPosition.clone().add(this.front));
        }

        @NotNull
        public Pair<@Nullable Shape, @Nullable Facing> getBack(@NotNull Instance instance, @NotNull BlockPosition blockPosition) {
            return this.getProperties(instance, blockPosition.clone().add(this.back));
        }

        @NotNull
        private Pair<@Nullable Shape, @Nullable Facing> getProperties(@NotNull Instance instance, @NotNull BlockPosition blockPosition) {
            Block block = instance.getBlock(blockPosition);
            if (block == null) {
                return Pair.of(null, null);
            }
            short stateId = instance.getBlockStateId(blockPosition);
            BlockAlternative alternative = block.getAlternative(stateId);
            try {
                Shape shape = Shape.valueOf(alternative.getProperty("shape").toUpperCase());
                Facing facing = Facing.valueOf(alternative.getProperty("facing").toUpperCase());
                return Pair.of(shape, facing);
            } catch (Exception ex) {
                return Pair.of(null, null);
            }
        }
    }

    @NotNull
    private Shape getShape(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @NotNull Facing facing) {
        Pair<Shape, Facing> front = facing.getFront(instance, blockPosition);
        Pair<Shape, Facing> back = facing.getBack(instance, blockPosition);
        Shape shape = this.getShapeFromSide(front, facing, Shape.INNER_RIGHT, Shape.INNER_LEFT);
        if (shape == null) {
            shape = this.getShapeFromSide(back, facing, Shape.OUTER_RIGHT, Shape.OUTER_LEFT);
        }
        return shape == null ? Shape.STRAIGHT : shape;
    }

    @Nullable
    private Shape getShapeFromSide(@NotNull Pair<Shape, Facing> side, @NotNull Facing facing, @NotNull Shape right, @NotNull Shape left) {
        if (side.getLeft() == null) {
            return null;
        }
        Facing sideFacing = side.getRight();
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
        float degrees = (player.getPosition().getYaw() - 90) % 360;
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