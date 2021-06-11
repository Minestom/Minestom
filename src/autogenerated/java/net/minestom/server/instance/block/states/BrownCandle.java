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
public final class BrownCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17566, "candles=1", "lit=true", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17567, "candles=1", "lit=true", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17568, "candles=1", "lit=false", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17569, "candles=1", "lit=false", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17570, "candles=2", "lit=true", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17571, "candles=2", "lit=true", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17572, "candles=2", "lit=false", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17573, "candles=2", "lit=false", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17574, "candles=3", "lit=true", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17575, "candles=3", "lit=true", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17576, "candles=3", "lit=false", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17577, "candles=3", "lit=false", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17578, "candles=4", "lit=true", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17579, "candles=4", "lit=true", "waterlogged=false"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17580, "candles=4", "lit=false", "waterlogged=true"));
        Block.BROWN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17581, "candles=4", "lit=false", "waterlogged=false"));
    }
}
