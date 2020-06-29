package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Snow {
	public static void initStates() {
		SNOW.addBlockAlternative(new BlockAlternative((short) 3921, "layers=1"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3922, "layers=2"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3923, "layers=3"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3924, "layers=4"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3925, "layers=5"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3926, "layers=6"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3927, "layers=7"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3928, "layers=8"));
	}
}
