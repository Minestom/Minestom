package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Hopper {
	public static void initStates() {
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6728, "enabled=true", "facing=down"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6729, "enabled=true", "facing=north"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6730, "enabled=true", "facing=south"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6731, "enabled=true", "facing=west"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6732, "enabled=true", "facing=east"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6733, "enabled=false", "facing=down"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6734, "enabled=false", "facing=north"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6735, "enabled=false", "facing=south"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6736, "enabled=false", "facing=west"));
		HOPPER.addBlockAlternative(new BlockAlternative((short) 6737, "enabled=false", "facing=east"));
	}
}
