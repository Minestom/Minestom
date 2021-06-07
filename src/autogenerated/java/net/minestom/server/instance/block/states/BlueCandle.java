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
public final class BlueCandle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17550, "candles=1", "lit=true", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17551, "candles=1", "lit=true", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17552, "candles=1", "lit=false", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17553, "candles=1", "lit=false", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17554, "candles=2", "lit=true", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17555, "candles=2", "lit=true", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17556, "candles=2", "lit=false", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17557, "candles=2", "lit=false", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17558, "candles=3", "lit=true", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17559, "candles=3", "lit=true", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17560, "candles=3", "lit=false", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17561, "candles=3", "lit=false", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17562, "candles=4", "lit=true", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17563, "candles=4", "lit=true", "waterlogged=false"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17564, "candles=4", "lit=false", "waterlogged=true"));
        Block.BLUE_CANDLE.addBlockAlternative(new BlockAlternative((short) 17565, "candles=4", "lit=false", "waterlogged=false"));
    }
}
