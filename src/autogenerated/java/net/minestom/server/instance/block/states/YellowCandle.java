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
public final class YellowCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17438, "candles=1", "lit=true", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17439, "candles=1", "lit=true", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17440, "candles=1", "lit=false", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17441, "candles=1", "lit=false", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17442, "candles=2", "lit=true", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17443, "candles=2", "lit=true", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17444, "candles=2", "lit=false", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17445, "candles=2", "lit=false", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17446, "candles=3", "lit=true", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17447, "candles=3", "lit=true", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17448, "candles=3", "lit=false", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17449, "candles=3", "lit=false", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17450, "candles=4", "lit=true", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17451, "candles=4", "lit=true", "waterlogged=false"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17452, "candles=4", "lit=false", "waterlogged=true"));
        Block.YELLOW_CANDLE.addBlockAlternative(new BlockAlternative((short) 17453, "candles=4", "lit=false", "waterlogged=false"));
    }
}
