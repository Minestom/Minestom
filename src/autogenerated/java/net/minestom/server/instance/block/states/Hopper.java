package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Hopper {
	public static void initStates() {
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6732, "enabled=true", "facing=down"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6733, "enabled=true", "facing=north"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6734, "enabled=true", "facing=south"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6735, "enabled=true", "facing=west"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6736, "enabled=true", "facing=east"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6737, "enabled=false", "facing=down"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6738, "enabled=false", "facing=north"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6739, "enabled=false", "facing=south"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6740, "enabled=false", "facing=west"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6741, "enabled=false", "facing=east"));
	}
}
