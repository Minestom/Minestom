package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Grindstone {
	public static void initStates() {
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14821, "face=floor", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14822, "face=floor", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14823, "face=floor", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14824, "face=floor", "facing=east"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14825, "face=wall", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14826, "face=wall", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14827, "face=wall", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14828, "face=wall", "facing=east"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14829, "face=ceiling", "facing=north"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14830, "face=ceiling", "facing=south"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14831, "face=ceiling", "facing=west"));
		GRINDSTONE.addBlockAlternative(new BlockAlternative((short) 14832, "face=ceiling", "facing=east"));
	}
}
