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
public final class CyanCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17518, "candles=1", "lit=true", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17519, "candles=1", "lit=true", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17520, "candles=1", "lit=false", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17521, "candles=1", "lit=false", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17522, "candles=2", "lit=true", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17523, "candles=2", "lit=true", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17524, "candles=2", "lit=false", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17525, "candles=2", "lit=false", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17526, "candles=3", "lit=true", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17527, "candles=3", "lit=true", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17528, "candles=3", "lit=false", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17529, "candles=3", "lit=false", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17530, "candles=4", "lit=true", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17531, "candles=4", "lit=true", "waterlogged=false"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17532, "candles=4", "lit=false", "waterlogged=true"));
        Block.CYAN_CANDLE.addBlockAlternative(new BlockAlternative((short) 17533, "candles=4", "lit=false", "waterlogged=false"));
    }
}
