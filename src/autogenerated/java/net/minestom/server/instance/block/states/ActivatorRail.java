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
public final class ActivatorRail {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6827, "powered=true", "shape=north_south"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6828, "powered=true", "shape=east_west"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6829, "powered=true", "shape=ascending_east"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6830, "powered=true", "shape=ascending_west"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6831, "powered=true", "shape=ascending_north"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6832, "powered=true", "shape=ascending_south"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6833, "powered=false", "shape=north_south"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6834, "powered=false", "shape=east_west"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6835, "powered=false", "shape=ascending_east"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6836, "powered=false", "shape=ascending_west"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6837, "powered=false", "shape=ascending_north"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6838, "powered=false", "shape=ascending_south"));
    }
}
