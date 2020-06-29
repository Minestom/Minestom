package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Cake {
	public static void initStates() {
		CAKE.addBlockAlternative(new BlockAlternative((short) 4024, "bites=0"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4025, "bites=1"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4026, "bites=2"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4027, "bites=3"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4028, "bites=4"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4029, "bites=5"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4030, "bites=6"));
	}
}
