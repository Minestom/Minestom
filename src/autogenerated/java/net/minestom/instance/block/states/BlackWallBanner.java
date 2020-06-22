package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BlackWallBanner {
	public static void initStates() {
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7677, "facing=north"));
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7678, "facing=south"));
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7679, "facing=west"));
		BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 7680, "facing=east"));
	}
}
