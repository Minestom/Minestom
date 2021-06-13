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
public final class PinkCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17470, "candles=1", "lit=true", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17471, "candles=1", "lit=true", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17472, "candles=1", "lit=false", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17473, "candles=1", "lit=false", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17474, "candles=2", "lit=true", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17475, "candles=2", "lit=true", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17476, "candles=2", "lit=false", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17477, "candles=2", "lit=false", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17478, "candles=3", "lit=true", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17479, "candles=3", "lit=true", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17480, "candles=3", "lit=false", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17481, "candles=3", "lit=false", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17482, "candles=4", "lit=true", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17483, "candles=4", "lit=true", "waterlogged=false"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17484, "candles=4", "lit=false", "waterlogged=true"));
        Block.PINK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17485, "candles=4", "lit=false", "waterlogged=false"));
    }
}
