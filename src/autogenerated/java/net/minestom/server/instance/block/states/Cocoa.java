package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Cocoa {
	public static void initStates() {
		COCOA.addBlockAlternative(new BlockAlternative((short) 5158, "age=0", "facing=north"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5159, "age=0", "facing=south"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5160, "age=0", "facing=west"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5161, "age=0", "facing=east"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5162, "age=1", "facing=north"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5163, "age=1", "facing=south"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5164, "age=1", "facing=west"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5165, "age=1", "facing=east"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5166, "age=2", "facing=north"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5167, "age=2", "facing=south"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5168, "age=2", "facing=west"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5169, "age=2", "facing=east"));
	}
}
