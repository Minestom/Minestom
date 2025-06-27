package net.minestom.demo.block.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

/**
 * https://gist.github.com/mworzala/0676c28343310458834d70ed29b11a37
 */
public class BedPlacementRule extends BlockPlacementRule {


    private static final String PROP_PART = "part";
    private static final String PROP_FACING = "facing";

    public BedPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull BlockChange mutation) {
        if( !(mutation instanceof BlockChange.Player mut)) {
            return mutation.block(); // not a player placement
        }
        var playerPosition = mut.player().getPosition();
        var facing = BlockFace.fromYaw(playerPosition.yaw());

        //todo bad code using instance directly
        if (!(mut.instance() instanceof Instance instance)) return mutation.block();

        var headPosition = mutation.blockPosition().relative(facing);
        if (!instance.getBlock(headPosition, Block.Getter.Condition.TYPE).isAir())
            return mutation.block();

        var headBlock = this.block.withProperty(PROP_PART, "head")
                .withProperty(PROP_FACING, facing.name().toLowerCase());
        instance.setBlock(headPosition, headBlock);

        return mut.block().withProperty(PROP_PART, "foot").withProperty(PROP_FACING, facing.name().toLowerCase());
    }
}
