package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Snow {
	public static void initStates() {
		SNOW.addBlockAlternative(new BlockAlternative((short) 3919, "layers=1"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3920, "layers=2"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3921, "layers=3"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3922, "layers=4"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3923, "layers=5"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3924, "layers=6"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3925, "layers=7"));
		SNOW.addBlockAlternative(new BlockAlternative((short) 3926, "layers=8"));
	}
}
