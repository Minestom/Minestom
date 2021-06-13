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
public final class BlackCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17614, "candles=1", "lit=true", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17615, "candles=1", "lit=true", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17616, "candles=1", "lit=false", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17617, "candles=1", "lit=false", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17618, "candles=2", "lit=true", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17619, "candles=2", "lit=true", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17620, "candles=2", "lit=false", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17621, "candles=2", "lit=false", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17622, "candles=3", "lit=true", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17623, "candles=3", "lit=true", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17624, "candles=3", "lit=false", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17625, "candles=3", "lit=false", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17626, "candles=4", "lit=true", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17627, "candles=4", "lit=true", "waterlogged=false"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17628, "candles=4", "lit=false", "waterlogged=true"));
        Block.BLACK_CANDLE.addBlockAlternative(new BlockAlternative((short) 17629, "candles=4", "lit=false", "waterlogged=false"));
    }
}
