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
public final class PurpleCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17534, "candles=1", "lit=true", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17535, "candles=1", "lit=true", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17536, "candles=1", "lit=false", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17537, "candles=1", "lit=false", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17538, "candles=2", "lit=true", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17539, "candles=2", "lit=true", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17540, "candles=2", "lit=false", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17541, "candles=2", "lit=false", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17542, "candles=3", "lit=true", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17543, "candles=3", "lit=true", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17544, "candles=3", "lit=false", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17545, "candles=3", "lit=false", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17546, "candles=4", "lit=true", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17547, "candles=4", "lit=true", "waterlogged=false"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17548, "candles=4", "lit=false", "waterlogged=true"));
        Block.PURPLE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17549, "candles=4", "lit=false", "waterlogged=false"));
    }
}
