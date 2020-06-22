package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Stonecutter {
	public static void initStates() {
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 11194, "facing=north"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 11195, "facing=south"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 11196, "facing=west"));
		STONECUTTER.addBlockAlternative(new BlockAlternative((short) 11197, "facing=east"));
	}
}
