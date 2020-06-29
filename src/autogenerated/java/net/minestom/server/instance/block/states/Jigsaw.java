package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Jigsaw {
	public static void initStates() {
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15739, "orientation=down_east"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15740, "orientation=down_north"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15741, "orientation=down_south"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15742, "orientation=down_west"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15743, "orientation=up_east"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15744, "orientation=up_north"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15745, "orientation=up_south"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15746, "orientation=up_west"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15747, "orientation=west_up"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15748, "orientation=east_up"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15749, "orientation=north_up"));
		JIGSAW.addBlockAlternative(new BlockAlternative((short) 15750, "orientation=south_up"));
	}
}
