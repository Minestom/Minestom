package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EndRod {
	public static void initStates() {
		END_ROD.addBlockAlternative(new BlockAlternative((short) 8522, "facing=north"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 8523, "facing=east"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 8524, "facing=south"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 8525, "facing=west"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 8526, "facing=up"));
		END_ROD.addBlockAlternative(new BlockAlternative((short) 8527, "facing=down"));
	}
}
