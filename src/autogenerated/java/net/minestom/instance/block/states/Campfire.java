package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Campfire {
	public static void initStates() {
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11232, "facing=north", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11233, "facing=north", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11234, "facing=north", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11235, "facing=north", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11236, "facing=north", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11237, "facing=north", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11238, "facing=north", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11239, "facing=north", "lit=false", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11240, "facing=south", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11241, "facing=south", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11242, "facing=south", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11243, "facing=south", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11244, "facing=south", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11245, "facing=south", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11246, "facing=south", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11247, "facing=south", "lit=false", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11248, "facing=west", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11249, "facing=west", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11250, "facing=west", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11251, "facing=west", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11252, "facing=west", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11253, "facing=west", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11254, "facing=west", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11255, "facing=west", "lit=false", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11256, "facing=east", "lit=true", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11257, "facing=east", "lit=true", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11258, "facing=east", "lit=true", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11259, "facing=east", "lit=true", "signal_fire=false", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11260, "facing=east", "lit=false", "signal_fire=true", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11261, "facing=east", "lit=false", "signal_fire=true", "waterlogged=false"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11262, "facing=east", "lit=false", "signal_fire=false", "waterlogged=true"));
		CAMPFIRE.addBlockAlternative(new BlockAlternative((short) 11263, "facing=east", "lit=false", "signal_fire=false", "waterlogged=false"));
	}
}
