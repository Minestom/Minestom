package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class WarpedButton {
	public static void initStates() {
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15503, "face=floor", "facing=north", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15504, "face=floor", "facing=north", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15505, "face=floor", "facing=south", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15506, "face=floor", "facing=south", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15507, "face=floor", "facing=west", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15508, "face=floor", "facing=west", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15509, "face=floor", "facing=east", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15510, "face=floor", "facing=east", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15511, "face=wall", "facing=north", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15512, "face=wall", "facing=north", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15513, "face=wall", "facing=south", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15514, "face=wall", "facing=south", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15515, "face=wall", "facing=west", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15516, "face=wall", "facing=west", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15517, "face=wall", "facing=east", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15518, "face=wall", "facing=east", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15519, "face=ceiling", "facing=north", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15520, "face=ceiling", "facing=north", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15521, "face=ceiling", "facing=south", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15522, "face=ceiling", "facing=south", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15523, "face=ceiling", "facing=west", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15524, "face=ceiling", "facing=west", "powered=false"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15525, "face=ceiling", "facing=east", "powered=true"));
		WARPED_BUTTON.addBlockAlternative(new BlockAlternative((short) 15526, "face=ceiling", "facing=east", "powered=false"));
	}
}
