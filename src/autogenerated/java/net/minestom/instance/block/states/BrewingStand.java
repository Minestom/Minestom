package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class BrewingStand {
	public static void initStates() {
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5117, "has_bottle_0=true", "has_bottle_1=true", "has_bottle_2=true"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5118, "has_bottle_0=true", "has_bottle_1=true", "has_bottle_2=false"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5119, "has_bottle_0=true", "has_bottle_1=false", "has_bottle_2=true"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5120, "has_bottle_0=true", "has_bottle_1=false", "has_bottle_2=false"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5121, "has_bottle_0=false", "has_bottle_1=true", "has_bottle_2=true"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5122, "has_bottle_0=false", "has_bottle_1=true", "has_bottle_2=false"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5123, "has_bottle_0=false", "has_bottle_1=false", "has_bottle_2=true"));
		BREWING_STAND.addBlockAlternative(new BlockAlternative((short) 5124, "has_bottle_0=false", "has_bottle_1=false", "has_bottle_2=false"));
	}
}
