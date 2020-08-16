package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaButton {
	public static void initStates() {
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6446, "face=floor", "facing=north", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6447, "face=floor", "facing=north", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6448, "face=floor", "facing=south", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6449, "face=floor", "facing=south", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6450, "face=floor", "facing=west", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6451, "face=floor", "facing=west", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6452, "face=floor", "facing=east", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6453, "face=floor", "facing=east", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6454, "face=wall", "facing=north", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6455, "face=wall", "facing=north", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6456, "face=wall", "facing=south", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6457, "face=wall", "facing=south", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6458, "face=wall", "facing=west", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6459, "face=wall", "facing=west", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6460, "face=wall", "facing=east", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6461, "face=wall", "facing=east", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6462, "face=ceiling", "facing=north", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6463, "face=ceiling", "facing=north", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6464, "face=ceiling", "facing=south", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6465, "face=ceiling", "facing=south", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6466, "face=ceiling", "facing=west", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6467, "face=ceiling", "facing=west", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6468, "face=ceiling", "facing=east", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 6469, "face=ceiling", "facing=east", "powered=false"));
	}
}
