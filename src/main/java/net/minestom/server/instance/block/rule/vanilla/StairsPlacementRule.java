package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityFacing;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class StairsPlacementRule extends BlockPlacementRule {

    private enum Shape {
        STRAIGHT,
        OUTER_LEFT,
        OUTER_RIGHT,
        INNER_LEFT,
        INNER_RIGHT
    }

    public StairsPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        return block.withProperty(
                "shape",
                getShape(
                        EntityFacing.valueOf(block.getProperty("facing").toUpperCase(Locale.ROOT)),
                        instance, blockPosition
                ).toString().toLowerCase(Locale.ROOT)
        );
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace,
                            @NotNull Point blockPosition, @NotNull Player player,
                            @NotNull Vec cursorPosition) {
        EntityFacing facing = player.getEntityFacing();
        BlockFace half = blockFace == BlockFace.TOP ?
                BlockFace.BOTTOM :
                (blockFace == BlockFace.BOTTOM ?
                        BlockFace.TOP :
                        (cursorPosition.y() > 0.5 ? BlockFace.TOP : BlockFace.BOTTOM)
                );
        Shape shape = getShape(facing, instance, blockPosition);
        return block.withProperties(Map.of(
                "facing", facing.toString().toLowerCase(Locale.ROOT),
                "half", half.toString().toLowerCase(Locale.ROOT),
                "shape", shape.toString().toLowerCase(Locale.ROOT),
                "waterlogged", "false"
        ));
    }

    private static Shape getShape(EntityFacing facing, Instance instance, Point blockPosition) {
        Block backBlock = instance.getBlock(blockPosition.add(facing.getDirection()));
        if (isStairs(backBlock)) { // outer
            EntityFacing backBlockFace = EntityFacing.valueOf(backBlock.getProperty("facing").toUpperCase(Locale.ROOT));
            if (backBlockFace == facing.onLeft()) {
                return Shape.OUTER_LEFT;
            } else if (backBlockFace == facing.onRight()) {
                return Shape.OUTER_RIGHT;
            }
        }
        Block frontBlock = instance.getBlock(blockPosition.sub(facing.getDirection()));
        if (isStairs(frontBlock)) { // inner
            EntityFacing backBlockFace = EntityFacing.valueOf(backBlock.getProperty("facing").toUpperCase(Locale.ROOT));
            if (backBlockFace == facing.onLeft()) {
                return Shape.INNER_LEFT;
            } else if (backBlockFace == facing.onRight()) {
                return Shape.INNER_RIGHT;
            }
        }
        return Shape.STRAIGHT;
    }

    public static boolean isStairs(Block block) {
        return block.name().endsWith("stairs"); // TODO change implementation
    }

}
