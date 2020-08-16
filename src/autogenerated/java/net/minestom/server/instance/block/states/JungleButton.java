package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleButton {
	public static void initStates() {
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6422, "face=floor", "facing=north", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6423, "face=floor", "facing=north", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6424, "face=floor", "facing=south", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6425, "face=floor", "facing=south", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6426, "face=floor", "facing=west", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6427, "face=floor", "facing=west", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6428, "face=floor", "facing=east", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6429, "face=floor", "facing=east", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6430, "face=wall", "facing=north", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6431, "face=wall", "facing=north", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6432, "face=wall", "facing=south", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6433, "face=wall", "facing=south", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6434, "face=wall", "facing=west", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6435, "face=wall", "facing=west", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6436, "face=wall", "facing=east", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6437, "face=wall", "facing=east", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6438, "face=ceiling", "facing=north", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6439, "face=ceiling", "facing=north", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6440, "face=ceiling", "facing=south", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6441, "face=ceiling", "facing=south", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6442, "face=ceiling", "facing=west", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6443, "face=ceiling", "facing=west", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6444, "face=ceiling", "facing=east", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6445, "face=ceiling", "facing=east", "powered=false"));
	}
}
