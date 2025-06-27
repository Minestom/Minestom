package net.minestom.demo.block.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class DripstonePlacementRule extends BlockPlacementRule {
    private static final String PROP_VERTICAL_DIRECTION = "vertical_direction"; // Tip, frustum, middle(0 or more), base
    private static final String PROP_THICKNESS = "thickness";

    public DripstonePlacementRule() {
        super(Block.POINTED_DRIPSTONE);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull BlockChange mutation) {
        if (!(mutation instanceof BlockChange.Player mut)) {
            return mutation.block(); // not a player placement
        }
        var blockFace = mut.blockFace();
        var direction = switch (blockFace) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> Objects.requireNonNullElse(mut.player().getPosition().direction(), Vec.ZERO).y() < 0.5 ? "up" : "down";
        };
        var thickness = getThickness(mut.instance(), mut.blockPosition(), direction.equals("up"));
        return block.withProperties(Map.of(
                PROP_VERTICAL_DIRECTION, direction,
                PROP_THICKNESS, thickness
        ));
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull BlockChange mutation) {
        var direction = mutation.block().getProperty(PROP_VERTICAL_DIRECTION).equals("up");
        var newThickness = getThickness(mutation.instance(), mutation.blockPosition(), direction);
        return mutation.block().withProperty(PROP_THICKNESS, newThickness);
    }

    private @NotNull String getThickness(@NotNull Block.Getter instance, @NotNull Point blockPosition, boolean direction) {
        var abovePosition = blockPosition.add(0, direction ? 1 : -1, 0);
        var aboveBlock = instance.getBlock(abovePosition, Block.Getter.Condition.TYPE);

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
        var belowPosition = blockPosition.add(0, direction ? -1 : 1, 0);
        var belowBlock = instance.getBlock(belowPosition, Block.Getter.Condition.TYPE);

        // If there is no dripstone below, it is always a base
        if (belowBlock.id() != Block.POINTED_DRIPSTONE.id())
            return "base";

        // Otherwise it is a middle
        return "middle";
    }

    @Override
    public int maxUpdateDistance() {
        return 2;
    }
}
