package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackBed {
	public static void initStates() {
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1289, "facing=north", "occupied=true", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1290, "facing=north", "occupied=true", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1291, "facing=north", "occupied=false", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1292, "facing=north", "occupied=false", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1293, "facing=south", "occupied=true", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1294, "facing=south", "occupied=true", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1295, "facing=south", "occupied=false", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1296, "facing=south", "occupied=false", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1297, "facing=west", "occupied=true", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1298, "facing=west", "occupied=true", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1299, "facing=west", "occupied=false", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1300, "facing=west", "occupied=false", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1301, "facing=east", "occupied=true", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1302, "facing=east", "occupied=true", "part=foot"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1303, "facing=east", "occupied=false", "part=head"));
		BLACK_BED.addBlockAlternative(new BlockAlternative((short) 1304, "facing=east", "occupied=false", "part=foot"));
	}
}
