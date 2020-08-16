package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AttachedPumpkinStem {
	public static void initStates() {
		ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4768, "facing=north"));
		ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4769, "facing=south"));
		ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4770, "facing=west"));
		ATTACHED_PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4771, "facing=east"));
	}
}
