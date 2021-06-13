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
public final class GrayCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17486, "candles=1", "lit=true", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17487, "candles=1", "lit=true", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17488, "candles=1", "lit=false", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17489, "candles=1", "lit=false", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17490, "candles=2", "lit=true", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17491, "candles=2", "lit=true", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17492, "candles=2", "lit=false", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17493, "candles=2", "lit=false", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17494, "candles=3", "lit=true", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17495, "candles=3", "lit=true", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17496, "candles=3", "lit=false", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17497, "candles=3", "lit=false", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17498, "candles=4", "lit=true", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17499, "candles=4", "lit=true", "waterlogged=false"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17500, "candles=4", "lit=false", "waterlogged=true"));
        Block.GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17501, "candles=4", "lit=false", "waterlogged=false"));
    }
}
