package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Repeater {
	public static void initStates() {
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4017, "delay=1", "facing=north", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4018, "delay=1", "facing=north", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4019, "delay=1", "facing=north", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4020, "delay=1", "facing=north", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4021, "delay=1", "facing=south", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4022, "delay=1", "facing=south", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4023, "delay=1", "facing=south", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4024, "delay=1", "facing=south", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4025, "delay=1", "facing=west", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4026, "delay=1", "facing=west", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4027, "delay=1", "facing=west", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4028, "delay=1", "facing=west", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4029, "delay=1", "facing=east", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4030, "delay=1", "facing=east", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4031, "delay=1", "facing=east", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4032, "delay=1", "facing=east", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4033, "delay=2", "facing=north", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4034, "delay=2", "facing=north", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4035, "delay=2", "facing=north", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4036, "delay=2", "facing=north", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4037, "delay=2", "facing=south", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4038, "delay=2", "facing=south", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4039, "delay=2", "facing=south", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4040, "delay=2", "facing=south", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4041, "delay=2", "facing=west", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4042, "delay=2", "facing=west", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4043, "delay=2", "facing=west", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4044, "delay=2", "facing=west", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4045, "delay=2", "facing=east", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4046, "delay=2", "facing=east", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4047, "delay=2", "facing=east", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4048, "delay=2", "facing=east", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4049, "delay=3", "facing=north", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4050, "delay=3", "facing=north", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4051, "delay=3", "facing=north", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4052, "delay=3", "facing=north", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4053, "delay=3", "facing=south", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4054, "delay=3", "facing=south", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4055, "delay=3", "facing=south", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4056, "delay=3", "facing=south", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4057, "delay=3", "facing=west", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4058, "delay=3", "facing=west", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4059, "delay=3", "facing=west", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4060, "delay=3", "facing=west", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4061, "delay=3", "facing=east", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4062, "delay=3", "facing=east", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4063, "delay=3", "facing=east", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4064, "delay=3", "facing=east", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4065, "delay=4", "facing=north", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4066, "delay=4", "facing=north", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4067, "delay=4", "facing=north", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4068, "delay=4", "facing=north", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4069, "delay=4", "facing=south", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4070, "delay=4", "facing=south", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4071, "delay=4", "facing=south", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4072, "delay=4", "facing=south", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4073, "delay=4", "facing=west", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4074, "delay=4", "facing=west", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4075, "delay=4", "facing=west", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4076, "delay=4", "facing=west", "locked=false", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4077, "delay=4", "facing=east", "locked=true", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4078, "delay=4", "facing=east", "locked=true", "powered=false"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4079, "delay=4", "facing=east", "locked=false", "powered=true"));
		REPEATER.addBlockAlternative(new BlockAlternative((short) 4080, "delay=4", "facing=east", "locked=false", "powered=false"));
	}
}
