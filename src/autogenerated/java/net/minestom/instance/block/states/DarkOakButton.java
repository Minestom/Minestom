package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DarkOakButton {
	public static void initStates() {
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5930, "face=floor", "facing=north", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5931, "face=floor", "facing=north", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5932, "face=floor", "facing=south", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5933, "face=floor", "facing=south", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5934, "face=floor", "facing=west", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5935, "face=floor", "facing=west", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5936, "face=floor", "facing=east", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5937, "face=floor", "facing=east", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5938, "face=wall", "facing=north", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5939, "face=wall", "facing=north", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5940, "face=wall", "facing=south", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5941, "face=wall", "facing=south", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5942, "face=wall", "facing=west", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5943, "face=wall", "facing=west", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5944, "face=wall", "facing=east", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5945, "face=wall", "facing=east", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5946, "face=ceiling", "facing=north", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5947, "face=ceiling", "facing=north", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5948, "face=ceiling", "facing=south", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5949, "face=ceiling", "facing=south", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5950, "face=ceiling", "facing=west", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5951, "face=ceiling", "facing=west", "powered=false"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5952, "face=ceiling", "facing=east", "powered=true"));
		DARK_OAK_BUTTON.addBlockAlternative(new BlockAlternative((short) 5953, "face=ceiling", "facing=east", "powered=false"));
	}
}
