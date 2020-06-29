package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleButton {
	public static void initStates() {
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5882, "face=floor", "facing=north", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5883, "face=floor", "facing=north", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5884, "face=floor", "facing=south", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5885, "face=floor", "facing=south", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5886, "face=floor", "facing=west", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5887, "face=floor", "facing=west", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5888, "face=floor", "facing=east", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5889, "face=floor", "facing=east", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5890, "face=wall", "facing=north", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5891, "face=wall", "facing=north", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5892, "face=wall", "facing=south", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5893, "face=wall", "facing=south", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5894, "face=wall", "facing=west", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5895, "face=wall", "facing=west", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5896, "face=wall", "facing=east", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5897, "face=wall", "facing=east", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5898, "face=ceiling", "facing=north", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5899, "face=ceiling", "facing=north", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5900, "face=ceiling", "facing=south", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5901, "face=ceiling", "facing=south", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5902, "face=ceiling", "facing=west", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5903, "face=ceiling", "facing=west", "powered=false"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5904, "face=ceiling", "facing=east", "powered=true"));
		JUNGLE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5905, "face=ceiling", "facing=east", "powered=false"));
	}
}
