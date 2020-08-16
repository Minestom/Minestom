package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleDoor {
	public static void initStates() {
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8870, "facing=north", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8871, "facing=north", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8872, "facing=north", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8873, "facing=north", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8874, "facing=north", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8875, "facing=north", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8876, "facing=north", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8877, "facing=north", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8878, "facing=north", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8879, "facing=north", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8880, "facing=north", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8881, "facing=north", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8882, "facing=north", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8883, "facing=north", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8884, "facing=north", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8885, "facing=north", "half=lower", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8886, "facing=south", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8887, "facing=south", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8888, "facing=south", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8889, "facing=south", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8890, "facing=south", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8891, "facing=south", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8892, "facing=south", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8893, "facing=south", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8894, "facing=south", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8895, "facing=south", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8896, "facing=south", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8897, "facing=south", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8898, "facing=south", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8899, "facing=south", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8900, "facing=south", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8901, "facing=south", "half=lower", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8902, "facing=west", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8903, "facing=west", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8904, "facing=west", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8905, "facing=west", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8906, "facing=west", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8907, "facing=west", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8908, "facing=west", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8909, "facing=west", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8910, "facing=west", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8911, "facing=west", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8912, "facing=west", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8913, "facing=west", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8914, "facing=west", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8915, "facing=west", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8916, "facing=west", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8917, "facing=west", "half=lower", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8918, "facing=east", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8919, "facing=east", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8920, "facing=east", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8921, "facing=east", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8922, "facing=east", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8923, "facing=east", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8924, "facing=east", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8925, "facing=east", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8926, "facing=east", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8927, "facing=east", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8928, "facing=east", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8929, "facing=east", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8930, "facing=east", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8931, "facing=east", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8932, "facing=east", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8933, "facing=east", "half=lower", "hinge=right", "open=false", "powered=false"));
	}
}
