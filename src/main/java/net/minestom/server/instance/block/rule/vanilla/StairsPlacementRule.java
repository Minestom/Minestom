package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public class StairsPlacementRule extends BlockPlacementRule {

    public StairsPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public short blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, short stateId) {
        return stateId;
    }

    @Override
    public short blockPlace(@NotNull Instance instance, @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition, @NotNull Player player) {
        Direction direction = this.getDirection(player);
        BlockFace half = BlockFace.BOTTOM;
        Shape shape = this.getShape(instance, blockPosition, direction);
        boolean waterlogged = false;
        return block.withProperties(
                "facing=".concat(direction.name().toLowerCase()),
                "half=".concat(half.name().toLowerCase()),
                "shape=".concat(shape.toString()),
                "waterlogged=".concat(Boolean.toString(waterlogged))
        );
    }

    private @NotNull Direction getDirection(@NotNull Player player) {
        float yaw = player.getPosition().getYaw();
        return Direction.valueOf(MathUtils.getHorizontalDirection(yaw).toString());
    }

    private @NotNull Shape getShape(@NotNull Instance instance, @NotNull BlockPosition position, @NotNull Direction direction) {
        Shape shape = this.getOffsetShape(instance, direction, this.getOffset(position, direction), Shape.OUTER_RIGHT, Shape.OUTER_LEFT);
        if (shape == null) {
            shape = this.getOffsetShape(instance, direction, this.getOffset(position, direction.opposite()), Shape.INNER_RIGHT, Shape.INNER_LEFT);
        }
        return shape == null ? Shape.STRAIGHT : shape;
    }

    private @Nullable Shape getOffsetShape(@NotNull Instance instance, @NotNull Direction direction, @NotNull BlockPosition position, @NotNull Shape right, @NotNull Shape left) {
        Block block = instance.getBlock(position);
        if (block.getName().toUpperCase().contains("STAIRS")) {
            short stateId = instance.getBlockStateId(position);
            BlockAlternative properties = block.getAlternative(stateId);
            if (properties == null) {
                return null;
            }
            Direction blockDirection = Direction.valueOf(properties.getProperty("facing").toUpperCase());
            if (!this.areAxesSame(blockDirection, direction)) {
                if (blockDirection.equals(this.rotate(direction))) {
                    return left;
                }
                return right;
            }
        }
        return null;
    }

    private @NotNull BlockPosition getOffset(@NotNull BlockPosition position, @NotNull Direction direction) {
        return position.clone().add(direction.normalX(), direction.normalY(), direction.normalZ());
    }

    private boolean areAxesSame(@NotNull Direction direction1, @NotNull Direction direction2) {
        Predicate<@NotNull Function<@NotNull Direction, @NotNull Integer>> compare = (axis) -> Math.abs(axis.apply(direction1)) == Math.abs(axis.apply(direction2));
        return compare.test(Direction::normalX) && compare.test(Direction::normalY) && compare.test(Direction::normalZ);
    }

    private @NotNull Direction rotate(@NotNull Direction direction) {
        switch (direction) {
            case NORTH:
                return Direction.WEST;
            case SOUTH:
                return Direction.EAST;
            case WEST:
                return Direction.SOUTH;
            case EAST:
                return Direction.NORTH;
            default:
                return null;
        }
    }

    private enum Shape {
        STRAIGHT("straight"),
        INNER_LEFT("inner_left"),
        INNER_RIGHT("inner_right"),
        OUTER_LEFT("outer_left"),
        OUTER_RIGHT("outer_right");

        private final String name;

        Shape(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}