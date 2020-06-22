package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PinkBed {
	public static void initStates() {
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1144, "facing=north", "occupied=true", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1145, "facing=north", "occupied=true", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1146, "facing=north", "occupied=false", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1147, "facing=north", "occupied=false", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1148, "facing=south", "occupied=true", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1149, "facing=south", "occupied=true", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1150, "facing=south", "occupied=false", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1151, "facing=south", "occupied=false", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1152, "facing=west", "occupied=true", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1153, "facing=west", "occupied=true", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1154, "facing=west", "occupied=false", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1155, "facing=west", "occupied=false", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1156, "facing=east", "occupied=true", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1157, "facing=east", "occupied=true", "part=foot"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1158, "facing=east", "occupied=false", "part=head"));
		PINK_BED.addBlockAlternative(new BlockAlternative((short) 1159, "facing=east", "occupied=false", "part=foot"));
	}
}
