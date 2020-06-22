package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BirchButton {
	public static void initStates() {
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5858, "face=floor", "facing=north", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5859, "face=floor", "facing=north", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5860, "face=floor", "facing=south", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5861, "face=floor", "facing=south", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5862, "face=floor", "facing=west", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5863, "face=floor", "facing=west", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5864, "face=floor", "facing=east", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5865, "face=floor", "facing=east", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5866, "face=wall", "facing=north", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5867, "face=wall", "facing=north", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5868, "face=wall", "facing=south", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5869, "face=wall", "facing=south", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5870, "face=wall", "facing=west", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5871, "face=wall", "facing=west", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5872, "face=wall", "facing=east", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5873, "face=wall", "facing=east", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5874, "face=ceiling", "facing=north", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5875, "face=ceiling", "facing=north", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5876, "face=ceiling", "facing=south", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5877, "face=ceiling", "facing=south", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5878, "face=ceiling", "facing=west", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5879, "face=ceiling", "facing=west", "powered=false"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5880, "face=ceiling", "facing=east", "powered=true"));
		BIRCH_BUTTON.addBlockAlternative(new BlockAlternative((short) 5881, "face=ceiling", "facing=east", "powered=false"));
	}
}
