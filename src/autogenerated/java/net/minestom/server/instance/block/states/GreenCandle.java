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
public final class GreenCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17582, "candles=1", "lit=true", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17583, "candles=1", "lit=true", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17584, "candles=1", "lit=false", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17585, "candles=1", "lit=false", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17586, "candles=2", "lit=true", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17587, "candles=2", "lit=true", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17588, "candles=2", "lit=false", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17589, "candles=2", "lit=false", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17590, "candles=3", "lit=true", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17591, "candles=3", "lit=true", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17592, "candles=3", "lit=false", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17593, "candles=3", "lit=false", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17594, "candles=4", "lit=true", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17595, "candles=4", "lit=true", "waterlogged=false"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17596, "candles=4", "lit=false", "waterlogged=true"));
        Block.GREEN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17597, "candles=4", "lit=false", "waterlogged=false"));
    }
}
