package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class LightBlueBed {
	public static void initStates() {
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1097, "facing=north", "occupied=true", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1098, "facing=north", "occupied=true", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1099, "facing=north", "occupied=false", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1100, "facing=north", "occupied=false", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1101, "facing=south", "occupied=true", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1102, "facing=south", "occupied=true", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1103, "facing=south", "occupied=false", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1104, "facing=south", "occupied=false", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1105, "facing=west", "occupied=true", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1106, "facing=west", "occupied=true", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1107, "facing=west", "occupied=false", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1108, "facing=west", "occupied=false", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1109, "facing=east", "occupied=true", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1110, "facing=east", "occupied=true", "part=foot"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1111, "facing=east", "occupied=false", "part=head"));
		LIGHT_BLUE_BED.addBlockAlternative(new BlockAlternative((short) 1112, "facing=east", "occupied=false", "part=foot"));
	}
}
