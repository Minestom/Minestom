package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BubbleColumn {
	public static void initStates() {
		BUBBLE_COLUMN.addBlockAlternative(new BlockAlternative((short) 9671, "drag=true"));
		BUBBLE_COLUMN.addBlockAlternative(new BlockAlternative((short) 9672, "drag=false"));
	}
}
