package net.minestom.demo.block.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DripstonePlacementRule extends BlockPlacementRule {
    private static final String PROP_VERTICAL_DIRECTION = "vertical_direction"; // Tip, frustum, middle(0 or more), base
    private static final String PROP_THICKNESS = "thickness";

    public DripstonePlacementRule() {
        super(Block.POINTED_DRIPSTONE);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = placementState.blockFace();
        var y = placementState.cursorPosition().y();
        return block.withProperty(PROP_VERTICAL_DIRECTION, switch (blockFace) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> y < 0.5 ? "up" : "down";
        });
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        return updateState.currentBlock()
                .withProperty(PROP_THICKNESS, getThickness(updateState));
    }

    private @NotNull String getThickness(@NotNull UpdateState updateState) {
        var direction = updateState.currentBlock().getProperty(PROP_VERTICAL_DIRECTION).equals("up");
        var abovePosition = updateState.blockPosition().add(0, direction ? 1 : -1, 0);
        var aboveBlock = updateState.instance().getBlock(abovePosition, Block.Getter.Condition.TYPE);

        // If there is no dripstone above, it is always a tip
        if (aboveBlock.id() != Block.POINTED_DRIPSTONE.id())
            return "tip";
        // If there is an opposite facing dripstone above, it is always a merged tip
        if ((direction ? "down" : "up").equals(aboveBlock.getProperty(PROP_VERTICAL_DIRECTION)))
            return "tip_merge";

        // If the dripstone above this is a tip, it is a frustum
        var aboveThickness = aboveBlock.getProperty(PROP_THICKNESS);
        if ("tip".equals(aboveThickness) || "tip_merge".equals(aboveThickness))
            return "frustum";

        // At this point we know that there is a dripstone above, and that the dripstone is facing the same direction.
        var belowPosition = updateState.blockPosition().add(0, direction ? -1 : 1, 0);
        var belowBlock = updateState.instance().getBlock(belowPosition, Block.Getter.Condition.TYPE);

        // If there is no dripstone below, it is always a base
        if (belowBlock.id() != Block.POINTED_DRIPSTONE.id())
            return "base";

        // Otherwise it is a middle
        return "middle";
    }
}

