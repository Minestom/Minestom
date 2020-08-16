package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class CrimsonButton {
	public static void initStates() {
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15487, "face=floor", "facing=north", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15488, "face=floor", "facing=north", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15489, "face=floor", "facing=south", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15490, "face=floor", "facing=south", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15491, "face=floor", "facing=west", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15492, "face=floor", "facing=west", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15493, "face=floor", "facing=east", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15494, "face=floor", "facing=east", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15495, "face=wall", "facing=north", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15496, "face=wall", "facing=north", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15497, "face=wall", "facing=south", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15498, "face=wall", "facing=south", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15499, "face=wall", "facing=west", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15500, "face=wall", "facing=west", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15501, "face=wall", "facing=east", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15502, "face=wall", "facing=east", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15503, "face=ceiling", "facing=north", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15504, "face=ceiling", "facing=north", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15505, "face=ceiling", "facing=south", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15506, "face=ceiling", "facing=south", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15507, "face=ceiling", "facing=west", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15508, "face=ceiling", "facing=west", "powered=false"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15509, "face=ceiling", "facing=east", "powered=true"));
		CRIMSON_BUTTON.addBlockAlternative(new BlockAlternative((short) 15510, "face=ceiling", "facing=east", "powered=false"));
	}
}
