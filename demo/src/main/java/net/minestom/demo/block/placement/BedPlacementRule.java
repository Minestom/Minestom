package net.minestom.demo.block.placement;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * https://gist.github.com/mworzala/0676c28343310458834d70ed29b11a37
 */
public class BedPlacementRule extends BlockPlacementRule {


    private static final String PROP_PART = "part";
    private static final String PROP_FACING = "facing";

    public BedPlacementRule(Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(PlacementState placementState) {
        var playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);
        var facing = BlockFace.fromYaw(playerPosition.yaw());

        //todo bad code using instance directly
        if (!(placementState.instance() instanceof Instance instance)) return null;

        var headPosition = placementState.placePosition().relative(facing);
        if (!instance.getBlock(headPosition, Block.Getter.Condition.TYPE).isAir())
            return null;

        var headBlock = this.block.withProperty(PROP_PART, "head")
                .withProperty(PROP_FACING, facing.name().toLowerCase());
        instance.setBlock(headPosition, headBlock);

        return headBlock.withProperty(PROP_PART, "foot");
    }
}
