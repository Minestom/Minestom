package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Comparator {
	public static void initStates() {
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6142, "facing=north", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6143, "facing=north", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6144, "facing=north", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6145, "facing=north", "mode=subtract", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6146, "facing=south", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6147, "facing=south", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6148, "facing=south", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6149, "facing=south", "mode=subtract", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6150, "facing=west", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6151, "facing=west", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6152, "facing=west", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6153, "facing=west", "mode=subtract", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6154, "facing=east", "mode=compare", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6155, "facing=east", "mode=compare", "powered=false"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6156, "facing=east", "mode=subtract", "powered=true"));
		COMPARATOR.addBlockAlternative(new BlockAlternative((short) 6157, "facing=east", "mode=subtract", "powered=false"));
	}
}
