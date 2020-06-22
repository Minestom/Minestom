package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EndPortalFrame {
	public static void initStates() {
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5130, "eye=true", "facing=north"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5131, "eye=true", "facing=south"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5132, "eye=true", "facing=west"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5133, "eye=true", "facing=east"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5134, "eye=false", "facing=north"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5135, "eye=false", "facing=south"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5136, "eye=false", "facing=west"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5137, "eye=false", "facing=east"));
	}
}
