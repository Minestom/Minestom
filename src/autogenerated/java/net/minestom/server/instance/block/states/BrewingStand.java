package net.minestom.server.instance.block.states;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;

/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(
        since = "forever",
        forRemoval = false
)
public final class BrewingStand {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5334, "has_bottle_0=true", "has_bottle_1=true", "has_bottle_2=true"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5335, "has_bottle_0=true", "has_bottle_1=true", "has_bottle_2=false"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5336, "has_bottle_0=true", "has_bottle_1=false", "has_bottle_2=true"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5337, "has_bottle_0=true", "has_bottle_1=false", "has_bottle_2=false"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5338, "has_bottle_0=false", "has_bottle_1=true", "has_bottle_2=true"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5339, "has_bottle_0=false", "has_bottle_1=true", "has_bottle_2=false"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5340, "has_bottle_0=false", "has_bottle_1=false", "has_bottle_2=true"));
        Block.BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5341, "has_bottle_0=false", "has_bottle_1=false", "has_bottle_2=false"));
    }
}
