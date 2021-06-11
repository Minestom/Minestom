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
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7029, "powered=true", "shape=north_south", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7030, "powered=true", "shape=north_south", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7031, "powered=true", "shape=east_west", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7032, "powered=true", "shape=east_west", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7033, "powered=true", "shape=ascending_east", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7034, "powered=true", "shape=ascending_east", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7035, "powered=true", "shape=ascending_west", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7036, "powered=true", "shape=ascending_west", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7037, "powered=true", "shape=ascending_north", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7038, "powered=true", "shape=ascending_north", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7039, "powered=true", "shape=ascending_south", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7040, "powered=true", "shape=ascending_south", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7041, "powered=false", "shape=north_south", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7042, "powered=false", "shape=north_south", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7043, "powered=false", "shape=east_west", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7044, "powered=false", "shape=east_west", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7045, "powered=false", "shape=ascending_east", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7046, "powered=false", "shape=ascending_east", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7047, "powered=false", "shape=ascending_west", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7048, "powered=false", "shape=ascending_west", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7049, "powered=false", "shape=ascending_north", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7050, "powered=false", "shape=ascending_north", "waterlogged=false"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7051, "powered=false", "shape=ascending_south", "waterlogged=true"));
        Block.ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 7052, "powered=false", "shape=ascending_south", "waterlogged=false"));
    }
}
