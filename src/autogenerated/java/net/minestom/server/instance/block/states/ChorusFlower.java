package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChorusFlower {
	public static void initStates() {
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9128, "age=0"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9129, "age=1"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9130, "age=2"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9131, "age=3"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9132, "age=4"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 9133, "age=5"));
	}
}
