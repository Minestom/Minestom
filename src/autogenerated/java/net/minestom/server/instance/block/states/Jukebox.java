package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Jukebox {
	public static void initStates() {
		JUKEBOX.addBlockAlternative(new BlockAlternative((short) 3964, "has_record=true"));
		JUKEBOX.addBlockAlternative(new BlockAlternative((short) 3965, "has_record=false"));
	}
}
