package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JackOLantern {
	public static void initStates() {
		JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4020, "facing=north"));
		JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4021, "facing=south"));
		JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4022, "facing=west"));
		JACK_O_LANTERN.addBlockAlternative(new BlockAlternative((short) 4023, "facing=east"));
	}
}
