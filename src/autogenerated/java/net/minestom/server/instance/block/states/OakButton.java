package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakButton {
	public static void initStates() {
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5810, "face=floor", "facing=north", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5811, "face=floor", "facing=north", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5812, "face=floor", "facing=south", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5813, "face=floor", "facing=south", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5814, "face=floor", "facing=west", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5815, "face=floor", "facing=west", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5816, "face=floor", "facing=east", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5817, "face=floor", "facing=east", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5818, "face=wall", "facing=north", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5819, "face=wall", "facing=north", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5820, "face=wall", "facing=south", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5821, "face=wall", "facing=south", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5822, "face=wall", "facing=west", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5823, "face=wall", "facing=west", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5824, "face=wall", "facing=east", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5825, "face=wall", "facing=east", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5826, "face=ceiling", "facing=north", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5827, "face=ceiling", "facing=north", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5828, "face=ceiling", "facing=south", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5829, "face=ceiling", "facing=south", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5830, "face=ceiling", "facing=west", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5831, "face=ceiling", "facing=west", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5832, "face=ceiling", "facing=east", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5833, "face=ceiling", "facing=east", "powered=false"));
	}
}
