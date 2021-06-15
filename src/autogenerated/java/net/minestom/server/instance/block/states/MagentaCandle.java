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
public final class MagentaCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17406, "candles=1", "lit=true", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17407, "candles=1", "lit=true", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17408, "candles=1", "lit=false", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17409, "candles=1", "lit=false", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17410, "candles=2", "lit=true", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17411, "candles=2", "lit=true", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17412, "candles=2", "lit=false", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17413, "candles=2", "lit=false", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17414, "candles=3", "lit=true", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17415, "candles=3", "lit=true", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17416, "candles=3", "lit=false", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17417, "candles=3", "lit=false", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17418, "candles=4", "lit=true", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17419, "candles=4", "lit=true", "waterlogged=false"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17420, "candles=4", "lit=false", "waterlogged=true"));
        Block.MAGENTA_CANDLE.addBlockAlternative(new BlockAlternative((short) 17421, "candles=4", "lit=false", "waterlogged=false"));
    }
}
