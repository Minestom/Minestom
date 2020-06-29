package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RespawnAnchor {
	public static void initStates() {
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15829, "charges=0"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15830, "charges=1"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15831, "charges=2"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15832, "charges=3"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15833, "charges=4"));
	}
}
