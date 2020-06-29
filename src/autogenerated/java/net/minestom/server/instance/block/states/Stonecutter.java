package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Stonecutter {
	public static void initStates() {
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14850, "facing=north"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14851, "facing=south"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14852, "facing=west"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 14853, "facing=east"));
	}
}
