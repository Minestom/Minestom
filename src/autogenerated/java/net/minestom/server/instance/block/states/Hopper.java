package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Hopper {
	public static void initStates() {
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6192, "enabled=true", "facing=down"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6193, "enabled=true", "facing=north"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6194, "enabled=true", "facing=south"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6195, "enabled=true", "facing=west"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6196, "enabled=true", "facing=east"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6197, "enabled=false", "facing=down"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6198, "enabled=false", "facing=north"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6199, "enabled=false", "facing=south"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6200, "enabled=false", "facing=west"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6201, "enabled=false", "facing=east"));
	}
}
