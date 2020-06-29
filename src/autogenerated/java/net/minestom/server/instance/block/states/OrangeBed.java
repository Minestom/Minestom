package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OrangeBed {
	public static void initStates() {
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1065, "facing=north", "occupied=true", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1066, "facing=north", "occupied=true", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1067, "facing=north", "occupied=false", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1068, "facing=north", "occupied=false", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1069, "facing=south", "occupied=true", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1070, "facing=south", "occupied=true", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1071, "facing=south", "occupied=false", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1072, "facing=south", "occupied=false", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1073, "facing=west", "occupied=true", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1074, "facing=west", "occupied=true", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1075, "facing=west", "occupied=false", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1076, "facing=west", "occupied=false", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1077, "facing=east", "occupied=true", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1078, "facing=east", "occupied=true", "part=foot"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1079, "facing=east", "occupied=false", "part=head"));
		ORANGE_BED.addBlockAlternative(new BlockAlternative((short) 1080, "facing=east", "occupied=false", "part=foot"));
	}
}
