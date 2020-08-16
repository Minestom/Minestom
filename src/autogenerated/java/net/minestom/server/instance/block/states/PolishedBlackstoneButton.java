package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PolishedBlackstoneButton {
	public static void initStates() {
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16761, "face=floor", "facing=north", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16762, "face=floor", "facing=north", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16763, "face=floor", "facing=south", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16764, "face=floor", "facing=south", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16765, "face=floor", "facing=west", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16766, "face=floor", "facing=west", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16767, "face=floor", "facing=east", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16768, "face=floor", "facing=east", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16769, "face=wall", "facing=north", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16770, "face=wall", "facing=north", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16771, "face=wall", "facing=south", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16772, "face=wall", "facing=south", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16773, "face=wall", "facing=west", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16774, "face=wall", "facing=west", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16775, "face=wall", "facing=east", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16776, "face=wall", "facing=east", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16777, "face=ceiling", "facing=north", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16778, "face=ceiling", "facing=north", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16779, "face=ceiling", "facing=south", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16780, "face=ceiling", "facing=south", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16781, "face=ceiling", "facing=west", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16782, "face=ceiling", "facing=west", "powered=false"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16783, "face=ceiling", "facing=east", "powered=true"));
		POLISHED_BLACKSTONE_BUTTON.addBlockAlternative(new BlockAlternative((short) 16784, "face=ceiling", "facing=east", "powered=false"));
	}
}
