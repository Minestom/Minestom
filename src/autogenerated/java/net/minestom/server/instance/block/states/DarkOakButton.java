package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkOakButton {
	public static void initStates() {
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6470, "face=floor", "facing=north", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6471, "face=floor", "facing=north", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6472, "face=floor", "facing=south", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6473, "face=floor", "facing=south", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6474, "face=floor", "facing=west", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6475, "face=floor", "facing=west", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6476, "face=floor", "facing=east", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6477, "face=floor", "facing=east", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6478, "face=wall", "facing=north", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6479, "face=wall", "facing=north", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6480, "face=wall", "facing=south", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6481, "face=wall", "facing=south", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6482, "face=wall", "facing=west", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6483, "face=wall", "facing=west", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6484, "face=wall", "facing=east", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6485, "face=wall", "facing=east", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6486, "face=ceiling", "facing=north", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6487, "face=ceiling", "facing=north", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6488, "face=ceiling", "facing=south", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6489, "face=ceiling", "facing=south", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6490, "face=ceiling", "facing=west", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6491, "face=ceiling", "facing=west", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6492, "face=ceiling", "facing=east", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 6493, "face=ceiling", "facing=east", "powered=false"));
	}
}
