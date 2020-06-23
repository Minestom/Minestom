package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpurPillar {
	public static void initStates() {
		PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 8599, "axis=x"));
		PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 8600, "axis=y"));
		PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 8601, "axis=z"));
	}
}
