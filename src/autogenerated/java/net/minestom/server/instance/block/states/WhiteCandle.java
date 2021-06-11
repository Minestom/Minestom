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
public final class WhiteCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17374, "candles=1", "lit=true", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17375, "candles=1", "lit=true", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17376, "candles=1", "lit=false", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17377, "candles=1", "lit=false", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17378, "candles=2", "lit=true", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17379, "candles=2", "lit=true", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17380, "candles=2", "lit=false", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17381, "candles=2", "lit=false", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17382, "candles=3", "lit=true", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17383, "candles=3", "lit=true", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17384, "candles=3", "lit=false", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17385, "candles=3", "lit=false", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17386, "candles=4", "lit=true", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17387, "candles=4", "lit=true", "waterlogged=false"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17388, "candles=4", "lit=false", "waterlogged=true"));
        Block.WHITE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17389, "candles=4", "lit=false", "waterlogged=false"));
    }
}
