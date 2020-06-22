package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OrangeGlazedTerracotta {
	public static void initStates() {
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8842, "facing=north"));
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8843, "facing=south"));
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8844, "facing=west"));
		ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 8845, "facing=east"));
	}
}
