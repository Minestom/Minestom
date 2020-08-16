package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceButton {
	public static void initStates() {
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6374, "face=floor", "facing=north", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6375, "face=floor", "facing=north", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6376, "face=floor", "facing=south", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6377, "face=floor", "facing=south", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6378, "face=floor", "facing=west", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6379, "face=floor", "facing=west", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6380, "face=floor", "facing=east", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6381, "face=floor", "facing=east", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6382, "face=wall", "facing=north", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6383, "face=wall", "facing=north", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6384, "face=wall", "facing=south", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6385, "face=wall", "facing=south", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6386, "face=wall", "facing=west", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6387, "face=wall", "facing=west", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6388, "face=wall", "facing=east", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6389, "face=wall", "facing=east", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6390, "face=ceiling", "facing=north", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6391, "face=ceiling", "facing=north", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6392, "face=ceiling", "facing=south", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6393, "face=ceiling", "facing=south", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6394, "face=ceiling", "facing=west", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6395, "face=ceiling", "facing=west", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6396, "face=ceiling", "facing=east", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 6397, "face=ceiling", "facing=east", "powered=false"));
	}
}
