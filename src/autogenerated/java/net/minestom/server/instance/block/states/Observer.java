package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Observer {
	public static void initStates() {
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8724, "facing=north", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8725, "facing=north", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8726, "facing=east", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8727, "facing=east", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8728, "facing=south", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8729, "facing=south", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8730, "facing=west", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8731, "facing=west", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8732, "facing=up", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8733, "facing=up", "powered=false"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8734, "facing=down", "powered=true"));
		OBSERVER.addBlockAlternative(new BlockAlternative((short) 8735, "facing=down", "powered=false"));
	}
}
