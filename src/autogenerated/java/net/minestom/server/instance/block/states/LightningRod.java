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
public final class LightningRod {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18520, "facing=north", "powered=true", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18521, "facing=north", "powered=true", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18522, "facing=north", "powered=false", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18523, "facing=north", "powered=false", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18524, "facing=east", "powered=true", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18525, "facing=east", "powered=true", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18526, "facing=east", "powered=false", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18527, "facing=east", "powered=false", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18528, "facing=south", "powered=true", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18529, "facing=south", "powered=true", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18530, "facing=south", "powered=false", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18531, "facing=south", "powered=false", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18532, "facing=west", "powered=true", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18533, "facing=west", "powered=true", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18534, "facing=west", "powered=false", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18535, "facing=west", "powered=false", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18536, "facing=up", "powered=true", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18537, "facing=up", "powered=true", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18538, "facing=up", "powered=false", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18539, "facing=up", "powered=false", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18540, "facing=down", "powered=true", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18541, "facing=down", "powered=true", "waterlogged=false"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18542, "facing=down", "powered=false", "waterlogged=true"));
        Block.LIGHTNING_ROD.addBlockAlternative(new BlockAlternative((short) 18543, "facing=down", "powered=false", "waterlogged=false"));
    }
}
