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
public final class Lectern {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15083, "facing=north", "has_book=true", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15084, "facing=north", "has_book=true", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15085, "facing=north", "has_book=false", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15086, "facing=north", "has_book=false", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15087, "facing=south", "has_book=true", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15088, "facing=south", "has_book=true", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15089, "facing=south", "has_book=false", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15090, "facing=south", "has_book=false", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15091, "facing=west", "has_book=true", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15092, "facing=west", "has_book=true", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15093, "facing=west", "has_book=false", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15094, "facing=west", "has_book=false", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15095, "facing=east", "has_book=true", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15096, "facing=east", "has_book=true", "powered=false"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15097, "facing=east", "has_book=false", "powered=true"));
        Block.LECTERN.addBlockAlternative(new BlockAlternative((short) 15098, "facing=east", "has_book=false", "powered=false"));
    }
}
