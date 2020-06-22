package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class QuartzPillar {
	public static void initStates() {
		QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6204, "axis=x"));
		QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6205, "axis=y"));
		QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6206, "axis=z"));
	}
}
