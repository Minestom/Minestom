package net.minestom.server.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.world.World;
import net.minestom.server.block.Block;
import net.minestom.server.block.BlockFace;
import net.minestom.server.block.BlockProperties;
import net.minestom.server.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class AxisPlacementRule extends BlockPlacementRule {

    public AxisPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull World world, @NotNull BlockPosition blockPosition, @NotNull Block block) {
        return block;
    }

    @Override
    public Block blockPlace(@NotNull World world,
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
