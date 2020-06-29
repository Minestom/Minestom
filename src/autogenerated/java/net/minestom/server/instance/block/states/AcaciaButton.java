package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class AcaciaButton {
	public static void initStates() {
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5906, "face=floor", "facing=north", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5907, "face=floor", "facing=north", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5908, "face=floor", "facing=south", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5909, "face=floor", "facing=south", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5910, "face=floor", "facing=west", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5911, "face=floor", "facing=west", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5912, "face=floor", "facing=east", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5913, "face=floor", "facing=east", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5914, "face=wall", "facing=north", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5915, "face=wall", "facing=north", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5916, "face=wall", "facing=south", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5917, "face=wall", "facing=south", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5918, "face=wall", "facing=west", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5919, "face=wall", "facing=west", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5920, "face=wall", "facing=east", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5921, "face=wall", "facing=east", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5922, "face=ceiling", "facing=north", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5923, "face=ceiling", "facing=north", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5924, "face=ceiling", "facing=south", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5925, "face=ceiling", "facing=south", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5926, "face=ceiling", "facing=west", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5927, "face=ceiling", "facing=west", "powered=false"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5928, "face=ceiling", "facing=east", "powered=true"));
		ACACIA_BUTTON.addBlockAlternative(new BlockAlternative((short) 5929, "face=ceiling", "facing=east", "powered=false"));
	}
}
