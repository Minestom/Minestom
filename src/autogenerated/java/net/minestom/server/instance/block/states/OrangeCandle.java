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
public final class OrangeCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17390, "candles=1", "lit=true", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17391, "candles=1", "lit=true", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17392, "candles=1", "lit=false", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17393, "candles=1", "lit=false", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17394, "candles=2", "lit=true", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17395, "candles=2", "lit=true", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17396, "candles=2", "lit=false", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17397, "candles=2", "lit=false", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17398, "candles=3", "lit=true", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17399, "candles=3", "lit=true", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17400, "candles=3", "lit=false", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17401, "candles=3", "lit=false", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17402, "candles=4", "lit=true", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17403, "candles=4", "lit=true", "waterlogged=false"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17404, "candles=4", "lit=false", "waterlogged=true"));
        Block.ORANGE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17405, "candles=4", "lit=false", "waterlogged=false"));
    }
}
