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
public final class RedCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17598, "candles=1", "lit=true", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17599, "candles=1", "lit=true", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17600, "candles=1", "lit=false", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17601, "candles=1", "lit=false", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17602, "candles=2", "lit=true", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17603, "candles=2", "lit=true", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17604, "candles=2", "lit=false", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17605, "candles=2", "lit=false", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17606, "candles=3", "lit=true", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17607, "candles=3", "lit=true", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17608, "candles=3", "lit=false", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17609, "candles=3", "lit=false", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17610, "candles=4", "lit=true", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17611, "candles=4", "lit=true", "waterlogged=false"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17612, "candles=4", "lit=false", "waterlogged=true"));
        Block.RED_CANDLE.addBlockAlternative(new BlockAlternative((short) 17613, "candles=4", "lit=false", "waterlogged=false"));
    }
}
