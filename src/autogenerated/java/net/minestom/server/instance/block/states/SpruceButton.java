package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class SpruceButton {
	public static void initStates() {
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5834, "face=floor", "facing=north", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5835, "face=floor", "facing=north", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5836, "face=floor", "facing=south", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5837, "face=floor", "facing=south", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5838, "face=floor", "facing=west", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5839, "face=floor", "facing=west", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5840, "face=floor", "facing=east", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5841, "face=floor", "facing=east", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5842, "face=wall", "facing=north", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5843, "face=wall", "facing=north", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5844, "face=wall", "facing=south", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5845, "face=wall", "facing=south", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5846, "face=wall", "facing=west", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5847, "face=wall", "facing=west", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5848, "face=wall", "facing=east", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5849, "face=wall", "facing=east", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5850, "face=ceiling", "facing=north", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5851, "face=ceiling", "facing=north", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5852, "face=ceiling", "facing=south", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5853, "face=ceiling", "facing=south", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5854, "face=ceiling", "facing=west", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5855, "face=ceiling", "facing=west", "powered=false"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5856, "face=ceiling", "facing=east", "powered=true"));
		SPRUCE_BUTTON.addBlockAlternative(new BlockAlternative((short) 5857, "face=ceiling", "facing=east", "powered=false"));
	}
}
