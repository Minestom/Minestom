package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBlackstoneButton {
	public static void initStates() {
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16753, "face=floor", "facing=north", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16754, "face=floor", "facing=north", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16755, "face=floor", "facing=south", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16756, "face=floor", "facing=south", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16757, "face=floor", "facing=west", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16758, "face=floor", "facing=west", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16759, "face=floor", "facing=east", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16760, "face=floor", "facing=east", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16761, "face=wall", "facing=north", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16762, "face=wall", "facing=north", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16763, "face=wall", "facing=south", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16764, "face=wall", "facing=south", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16765, "face=wall", "facing=west", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16766, "face=wall", "facing=west", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16767, "face=wall", "facing=east", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16768, "face=wall", "facing=east", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16769, "face=ceiling", "facing=north", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16770, "face=ceiling", "facing=north", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16771, "face=ceiling", "facing=south", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16772, "face=ceiling", "facing=south", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16773, "face=ceiling", "facing=west", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16774, "face=ceiling", "facing=west", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16775, "face=ceiling", "facing=east", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16776, "face=ceiling", "facing=east", "powered=false"));
	}
}
