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
public final class LightGrayCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17502, "candles=1", "lit=true", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17503, "candles=1", "lit=true", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17504, "candles=1", "lit=false", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17505, "candles=1", "lit=false", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17506, "candles=2", "lit=true", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17507, "candles=2", "lit=true", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17508, "candles=2", "lit=false", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17509, "candles=2", "lit=false", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17510, "candles=3", "lit=true", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17511, "candles=3", "lit=true", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17512, "candles=3", "lit=false", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17513, "candles=3", "lit=false", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17514, "candles=4", "lit=true", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17515, "candles=4", "lit=true", "waterlogged=false"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17516, "candles=4", "lit=false", "waterlogged=true"));
        Block.LIGHT_GRAY_CANDLE.addBlockAlternative(new BlockAlternative((short) 17517, "candles=4", "lit=false", "waterlogged=false"));
    }
}
