package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Grindstone {
	public static void initStates() {
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14825, "face=floor", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14826, "face=floor", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14827, "face=floor", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14828, "face=floor", "facing=east"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14829, "face=wall", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14830, "face=wall", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14831, "face=wall", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14832, "face=wall", "facing=east"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14833, "face=ceiling", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14834, "face=ceiling", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14835, "face=ceiling", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14836, "face=ceiling", "facing=east"));
	}
}
