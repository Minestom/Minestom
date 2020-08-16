package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class RespawnAnchor {
	public static void initStates() {
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15837, "charges=0"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15838, "charges=1"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15839, "charges=2"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15840, "charges=3"));
		RESPAWN_ANCHOR.addBlockAlternative(new BlockAlternative((short) 15841, "charges=4"));
	}
}
