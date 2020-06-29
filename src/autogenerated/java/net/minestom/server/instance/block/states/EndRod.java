package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EndRod {
	public static void initStates() {
		END_ROD.addBlockAlternative(new BlockAlternative((short) 9058, "facing=north"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 9059, "facing=east"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 9060, "facing=south"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 9061, "facing=west"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 9062, "facing=up"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 9063, "facing=down"));
	}
}
