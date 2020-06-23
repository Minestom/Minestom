package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CarvedPumpkin {
	public static void initStates() {
		CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4002, "facing=north"));
		CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4003, "facing=south"));
		CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4004, "facing=west"));
		CARVED_PUMPKIN.addBlockAlternative(new BlockAlternative((short) 4005, "facing=east"));
	}
}
