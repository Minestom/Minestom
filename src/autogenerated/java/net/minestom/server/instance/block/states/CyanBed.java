package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CyanBed {
	public static void initStates() {
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1193, "facing=north", "occupied=true", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1194, "facing=north", "occupied=true", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1195, "facing=north", "occupied=false", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1196, "facing=north", "occupied=false", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1197, "facing=south", "occupied=true", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1198, "facing=south", "occupied=true", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1199, "facing=south", "occupied=false", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1200, "facing=south", "occupied=false", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1201, "facing=west", "occupied=true", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1202, "facing=west", "occupied=true", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1203, "facing=west", "occupied=false", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1204, "facing=west", "occupied=false", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1205, "facing=east", "occupied=true", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1206, "facing=east", "occupied=true", "part=foot"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1207, "facing=east", "occupied=false", "part=head"));
		CYAN_BED.addBlockAlternative(new BlockAlternative((short) 1208, "facing=east", "occupied=false", "part=foot"));
	}
}
