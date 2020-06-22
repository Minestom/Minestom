package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ChorusFlower {
	public static void initStates() {
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 8592, "age=0"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 8593, "age=1"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 8594, "age=2"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 8595, "age=3"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 8596, "age=4"));
		CHORUS_FLOWER.addBlockAlternative(new BlockAlternative((short) 8597, "age=5"));
	}
}
