package net.minestom.demo.block.placement;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
<<<<<<< HEAD
=======
import net.minestom.server.instance.block.BlockChange;
>>>>>>> cc02c79fb (Cleanup)
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
<<<<<<< HEAD
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);
=======
    public @NotNull Block blockPlace(@NotNull BlockChange mutation) {
        if( !(mutation instanceof BlockChange.Player mut)) {
            return mutation.block(); // not a player placement
        }
        var playerPosition = mut.player().getPosition();
>>>>>>> cc02c79fb (Cleanup)
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
