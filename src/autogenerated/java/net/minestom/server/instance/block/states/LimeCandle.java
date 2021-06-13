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
public final class LimeCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17454, "candles=1", "lit=true", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17455, "candles=1", "lit=true", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17456, "candles=1", "lit=false", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17457, "candles=1", "lit=false", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17458, "candles=2", "lit=true", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17459, "candles=2", "lit=true", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17460, "candles=2", "lit=false", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17461, "candles=2", "lit=false", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17462, "candles=3", "lit=true", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17463, "candles=3", "lit=true", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17464, "candles=3", "lit=false", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17465, "candles=3", "lit=false", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17466, "candles=4", "lit=true", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17467, "candles=4", "lit=true", "waterlogged=false"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17468, "candles=4", "lit=false", "waterlogged=true"));
        Block.LIME_CANDLE.addBlockAlternative(new BlockAlternative((short) 17469, "candles=4", "lit=false", "waterlogged=false"));
    }
}
