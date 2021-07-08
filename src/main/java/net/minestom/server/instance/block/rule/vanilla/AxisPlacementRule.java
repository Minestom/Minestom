package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public class AxisPlacementRule extends BlockPlacementRule {

    public AxisPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        return block;
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Point blockPosition,
                            @NotNull Player pl) {
        String axis = "y";
        if (blockFace == BlockFace.WEST || blockFace == BlockFace.EAST) {
            axis = "x";
        } else if (blockFace == BlockFace.SOUTH || blockFace == BlockFace.NORTH) {
            axis = "z";
        }
        return block.withProperty("axis", axis);
    }
}
