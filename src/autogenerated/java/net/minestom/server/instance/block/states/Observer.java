package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Observer {
	public static void initStates() {
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9264, "facing=north", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9265, "facing=north", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9266, "facing=east", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9267, "facing=east", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9268, "facing=south", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9269, "facing=south", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9270, "facing=west", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9271, "facing=west", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9272, "facing=up", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9273, "facing=up", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9274, "facing=down", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9275, "facing=down", "powered=false"));
	}
}
