package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpurPillar {
	public static void initStates() {
		PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 9135, "axis=x"));
		PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 9136, "axis=y"));
		PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 9137, "axis=z"));
	}
}
