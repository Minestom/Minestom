package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Cocoa {
	public static void initStates() {
		COCOA.addBlockAlternative(new BlockAlternative((short) 5142, "age=0", "facing=north"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5143, "age=0", "facing=south"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5144, "age=0", "facing=west"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5145, "age=0", "facing=east"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5146, "age=1", "facing=north"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5147, "age=1", "facing=south"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5148, "age=1", "facing=west"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5149, "age=1", "facing=east"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5150, "age=2", "facing=north"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5151, "age=2", "facing=south"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5152, "age=2", "facing=west"));
		COCOA.addBlockAlternative(new BlockAlternative((short) 5153, "age=2", "facing=east"));
	}
}
