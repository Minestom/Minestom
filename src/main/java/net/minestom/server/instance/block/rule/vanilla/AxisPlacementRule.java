package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockProperties;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class AxisPlacementRule extends BlockPlacementRule {

    protected final Block block;

    public AxisPlacementRule(Block block) {
        super(block);
        this.block = block;
    }

    @Override
    public Block blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, Block block) {
        return block;
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                            @NotNull Player pl) {
        String axis = "Y";
        if (blockFace == BlockFace.WEST || blockFace == BlockFace.EAST) {
            axis = "X";
        } else if (blockFace == BlockFace.SOUTH || blockFace == BlockFace.NORTH) {
            axis = "Z";
        }
        return block.withProperty(BlockProperties.AXIS, axis);
    }

}
