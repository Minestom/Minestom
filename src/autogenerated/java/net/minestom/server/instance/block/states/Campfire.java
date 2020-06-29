package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Campfire {
	public static void initStates() {
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14890, "facing=north", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14891, "facing=north", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14892, "facing=north", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14893, "facing=north", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14894, "facing=north", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14895, "facing=north", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14896, "facing=north", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14897, "facing=north", "lit=false", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14898, "facing=south", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14899, "facing=south", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14900, "facing=south", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14901, "facing=south", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14902, "facing=south", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14903, "facing=south", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14904, "facing=south", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14905, "facing=south", "lit=false", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14906, "facing=west", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14907, "facing=west", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14908, "facing=west", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14909, "facing=west", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14910, "facing=west", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14911, "facing=west", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14912, "facing=west", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14913, "facing=west", "lit=false", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14914, "facing=east", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14915, "facing=east", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14916, "facing=east", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14917, "facing=east", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14918, "facing=east", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14919, "facing=east", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14920, "facing=east", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 14921, "facing=east", "lit=false", "signal_fire=false", "waterlogged=false"));
	}
}
