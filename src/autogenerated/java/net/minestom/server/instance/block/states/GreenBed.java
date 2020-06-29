package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class GreenBed {
	public static void initStates() {
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1257, "facing=north", "occupied=true", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1258, "facing=north", "occupied=true", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1259, "facing=north", "occupied=false", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1260, "facing=north", "occupied=false", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1261, "facing=south", "occupied=true", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1262, "facing=south", "occupied=true", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1263, "facing=south", "occupied=false", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1264, "facing=south", "occupied=false", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1265, "facing=west", "occupied=true", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1266, "facing=west", "occupied=true", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1267, "facing=west", "occupied=false", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1268, "facing=west", "occupied=false", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1269, "facing=east", "occupied=true", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1270, "facing=east", "occupied=true", "part=foot"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1271, "facing=east", "occupied=false", "part=head"));
		GREEN_BED.addBlockAlternative(new BlockAlternative((short) 1272, "facing=east", "occupied=false", "part=foot"));
	}
}
