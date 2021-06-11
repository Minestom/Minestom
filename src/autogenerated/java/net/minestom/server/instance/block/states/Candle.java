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
public final class Candle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17358, "candles=1", "lit=true", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17359, "candles=1", "lit=true", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17360, "candles=1", "lit=false", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17361, "candles=1", "lit=false", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17362, "candles=2", "lit=true", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17363, "candles=2", "lit=true", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17364, "candles=2", "lit=false", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17365, "candles=2", "lit=false", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17366, "candles=3", "lit=true", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17367, "candles=3", "lit=true", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17368, "candles=3", "lit=false", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17369, "candles=3", "lit=false", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17370, "candles=4", "lit=true", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17371, "candles=4", "lit=true", "waterlogged=false"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17372, "candles=4", "lit=false", "waterlogged=true"));
        Block.CANDLE.addBlockAlternative(new BlockAlternative((short) 17373, "candles=4", "lit=false", "waterlogged=false"));
    }
}
