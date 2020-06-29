package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PurpleBed {
	public static void initStates() {
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1209, "facing=north", "occupied=true", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1210, "facing=north", "occupied=true", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1211, "facing=north", "occupied=false", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1212, "facing=north", "occupied=false", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1213, "facing=south", "occupied=true", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1214, "facing=south", "occupied=true", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1215, "facing=south", "occupied=false", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1216, "facing=south", "occupied=false", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1217, "facing=west", "occupied=true", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1218, "facing=west", "occupied=true", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1219, "facing=west", "occupied=false", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1220, "facing=west", "occupied=false", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1221, "facing=east", "occupied=true", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1222, "facing=east", "occupied=true", "part=foot"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1223, "facing=east", "occupied=false", "part=head"));
		PURPLE_BED.addBlockAlternative(new BlockAlternative((short) 1224, "facing=east", "occupied=false", "part=foot"));
	}
}
