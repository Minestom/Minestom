package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Observer {
	public static void initStates() {
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9260, "facing=north", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9261, "facing=north", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9262, "facing=east", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9263, "facing=east", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9264, "facing=south", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9265, "facing=south", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9266, "facing=west", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9267, "facing=west", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9268, "facing=up", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9269, "facing=up", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9270, "facing=down", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 9271, "facing=down", "powered=false"));
	}
}
