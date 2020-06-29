package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class StickyPiston {
	public static void initStates() {
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1329, "extended=true", "facing=north"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1330, "extended=true", "facing=east"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1331, "extended=true", "facing=south"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1332, "extended=true", "facing=west"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1333, "extended=true", "facing=up"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1334, "extended=true", "facing=down"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1335, "extended=false", "facing=north"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1336, "extended=false", "facing=east"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1337, "extended=false", "facing=south"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1338, "extended=false", "facing=west"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1339, "extended=false", "facing=up"));
		STICKY_PISTON.addBlockAlternative(new BlockAlternative((short) 1340, "extended=false", "facing=down"));
	}
}
