package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class OakButton {
	public static void initStates() {
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6350, "face=floor", "facing=north", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6351, "face=floor", "facing=north", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6352, "face=floor", "facing=south", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6353, "face=floor", "facing=south", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6354, "face=floor", "facing=west", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6355, "face=floor", "facing=west", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6356, "face=floor", "facing=east", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6357, "face=floor", "facing=east", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6358, "face=wall", "facing=north", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6359, "face=wall", "facing=north", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6360, "face=wall", "facing=south", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6361, "face=wall", "facing=south", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6362, "face=wall", "facing=west", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6363, "face=wall", "facing=west", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6364, "face=wall", "facing=east", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6365, "face=wall", "facing=east", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6366, "face=ceiling", "facing=north", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6367, "face=ceiling", "facing=north", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6368, "face=ceiling", "facing=south", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6369, "face=ceiling", "facing=south", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6370, "face=ceiling", "facing=west", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6371, "face=ceiling", "facing=west", "powered=false"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6372, "face=ceiling", "facing=east", "powered=true"));
		OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6373, "face=ceiling", "facing=east", "powered=false"));
	}
}
