package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class EndPortalFrame {
	public static void initStates() {
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5146, "eye=true", "facing=north"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5147, "eye=true", "facing=south"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5148, "eye=true", "facing=west"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5149, "eye=true", "facing=east"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5150, "eye=false", "facing=north"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5151, "eye=false", "facing=south"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5152, "eye=false", "facing=west"));
		END_PORTAL_FRAME.addBlockAlternative(new BlockAlternative((short) 5153, "eye=false", "facing=east"));
	}
}
