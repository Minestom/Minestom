package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Bamboo {
	public static void initStates() {
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9116, "age=0", "leaves=none", "stage=0"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9117, "age=0", "leaves=none", "stage=1"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9118, "age=0", "leaves=small", "stage=0"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9119, "age=0", "leaves=small", "stage=1"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9120, "age=0", "leaves=large", "stage=0"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9121, "age=0", "leaves=large", "stage=1"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9122, "age=1", "leaves=none", "stage=0"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9123, "age=1", "leaves=none", "stage=1"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9124, "age=1", "leaves=small", "stage=0"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9125, "age=1", "leaves=small", "stage=1"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9126, "age=1", "leaves=large", "stage=0"));
		BAMBOO.addBlockAlternative(new BlockAlternative((short) 9127, "age=1", "leaves=large", "stage=1"));
	}
}
