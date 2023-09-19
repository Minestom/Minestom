package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StairsPlacementRule extends BlockPlacementRule {

    public StairsPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        Shape shape = getShape(instance, blockPosition, getFacing(block), getHalf(block));
        return block.withProperty("shape", shape.toString().toLowerCase());
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace,
                            @NotNull Point blockPosition, @NotNull Player player, @NotNull Point cursorPosition) {
        BlockFace facing = getFacing(player);
        Point pos = blockPosition.relative(blockFace);

        BlockFace half = (cursorPosition.y() > 0.5 ? BlockFace.TOP : BlockFace.BOTTOM);
        if (blockFace == BlockFace.BOTTOM || blockFace == BlockFace.TOP) {
            half = blockFace.getOppositeFace();
        }
        Shape shape = getShape(instance, pos, facing, half);

        String waterlogged = "false";
        return block.withProperties(Map.of(
                "facing", facing.toString().toLowerCase(),
                "half", half.toString().toLowerCase(),
                "shape", shape.toString().toLowerCase(),
                "waterlogged", waterlogged));
    }

    private enum Shape {
        STRAIGHT,
        OUTER_LEFT,
        OUTER_RIGHT,
        INNER_LEFT,
        INNER_RIGHT
    }

    @Nullable
    private static BlockFace getFacing(Block block) {
        try {
            return BlockFace.valueOf(block.getProperty("facing").toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }

    @Nullable
    private static BlockFace getHalf(Block block) {
        try {
            return BlockFace.valueOf(block.getProperty("half").toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }

    private @NotNull BlockFace getFacing(@NotNull Player player) {
        float degrees = (player.getPosition().yaw() - 90) % 360;
        if (degrees < 0) {
            degrees += 360;
        }
        if (0 <= degrees && degrees < 45) {
            return BlockFace.WEST;
        } else if (45 <= degrees && degrees < 135) {
            return BlockFace.NORTH;
        } else if (135 <= degrees && degrees < 225) {
            return BlockFace.EAST;
        } else if (225 <= degrees && degrees < 315) {
            return BlockFace.SOUTH;
        } else { // 315 <= degrees && degrees < 360
            return BlockFace.WEST;
        }
    }

    private Shape getShape(Instance instance, Point blockPosition, BlockFace facing, BlockFace half) {
        Shape shape = getShapeFromSide(instance, blockPosition, facing, true, Shape.OUTER_LEFT, Shape.OUTER_RIGHT, half);
        if (shape == null)
            shape = getShapeFromSide(instance, blockPosition, facing, false, Shape.INNER_LEFT, Shape.INNER_RIGHT, half);

        return shape == null ? Shape.STRAIGHT : shape;
    }

    private Shape getShapeFromSide(Instance instance, Point blockPosition, BlockFace facing, boolean front,
                                   Shape left, Shape right, BlockFace half) {
        Block neighbor = instance.getBlock(blockPosition.relative(front ? facing : facing.getOppositeFace()));
        if (!isStairsBlock(neighbor)) return null;
        if (half != getHalf(neighbor)) return null;

        BlockFace otherFacing = getFacing(neighbor);
        if (otherFacing == null) return null;
        // Skip faces with equal or opposite directions
        if (sameAxis(otherFacing, facing)) return null;

        if (checkNeighbor(instance, blockPosition, facing, front ? otherFacing.getOppositeFace() : otherFacing, half)) {
            return otherFacing == rotate(facing) ? left : right;
        }

        return null;
    }

    private boolean sameAxis(BlockFace face1, BlockFace face2) {
        Direction dir1 = face1.toDirection();
        Direction dir2 = face2.toDirection();
        // x
        if (dir1.normalX() != 0 && dir2.normalX() != 0)
            return true;
        // y
        if (dir1.normalY() != 0 && dir2.normalY() != 0)
            return true;
        // z
        if (dir1.normalZ() != 0 && dir2.normalZ() != 0)
            return true;

        return false;
    }

    private BlockFace rotate(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.WEST;
            case EAST -> BlockFace.NORTH;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            default -> throw new IllegalStateException("Invalid block face");
        };
    }

    private boolean checkNeighbor(Instance world, Point pos, BlockFace facing, BlockFace otherFacing, BlockFace half) {
        Block neighbor = world.getBlock(pos.relative(otherFacing));
        return !isStairsBlock(neighbor) || getFacing(neighbor) != facing || half != getHalf(neighbor);
    }

    public boolean isStairsBlock(Block block) {
        return block.name().contains("stairs");
    }

}
