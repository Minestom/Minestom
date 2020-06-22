package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Cake {
	public static void initStates() {
		CAKE.addBlockAlternative(new BlockAlternative((short) 4010, "bites=0"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4011, "bites=1"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4012, "bites=2"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4013, "bites=3"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4014, "bites=4"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4015, "bites=5"));
		CAKE.addBlockAlternative(new BlockAlternative((short) 4016, "bites=6"));
	}
}
