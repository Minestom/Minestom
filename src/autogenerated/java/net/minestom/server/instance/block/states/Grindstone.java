package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Grindstone {
	public static void initStates() {
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11165, "face=floor", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11166, "face=floor", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11167, "face=floor", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11168, "face=floor", "facing=east"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11169, "face=wall", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11170, "face=wall", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11171, "face=wall", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11172, "face=wall", "facing=east"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11173, "face=ceiling", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11174, "face=ceiling", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11175, "face=ceiling", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 11176, "face=ceiling", "facing=east"));
	}
}
