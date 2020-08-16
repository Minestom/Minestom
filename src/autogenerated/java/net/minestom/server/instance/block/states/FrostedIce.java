package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class FrostedIce {
	public static void initStates() {
		FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9253, "age=0"));
		FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9254, "age=1"));
		FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9255, "age=2"));
		FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9256, "age=3"));
	}
}
