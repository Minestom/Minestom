package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonButton {
	public static void initStates() {
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15479, "face=floor", "facing=north", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15480, "face=floor", "facing=north", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15481, "face=floor", "facing=south", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15482, "face=floor", "facing=south", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15483, "face=floor", "facing=west", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15484, "face=floor", "facing=west", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15485, "face=floor", "facing=east", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15486, "face=floor", "facing=east", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15487, "face=wall", "facing=north", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15488, "face=wall", "facing=north", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15489, "face=wall", "facing=south", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15490, "face=wall", "facing=south", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15491, "face=wall", "facing=west", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15492, "face=wall", "facing=west", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15493, "face=wall", "facing=east", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15494, "face=wall", "facing=east", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15495, "face=ceiling", "facing=north", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15496, "face=ceiling", "facing=north", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15497, "face=ceiling", "facing=south", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15498, "face=ceiling", "facing=south", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15499, "face=ceiling", "facing=west", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15500, "face=ceiling", "facing=west", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15501, "face=ceiling", "facing=east", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15502, "face=ceiling", "facing=east", "powered=false"));
	}
}
