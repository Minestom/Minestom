package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class IronDoor {
	public static void initStates() {
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3809, "facing=north", "half=upper", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3810, "facing=north", "half=upper", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3811, "facing=north", "half=upper", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3812, "facing=north", "half=upper", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3813, "facing=north", "half=upper", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3814, "facing=north", "half=upper", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3815, "facing=north", "half=upper", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3816, "facing=north", "half=upper", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3817, "facing=north", "half=lower", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3818, "facing=north", "half=lower", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3819, "facing=north", "half=lower", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3820, "facing=north", "half=lower", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3821, "facing=north", "half=lower", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3822, "facing=north", "half=lower", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3823, "facing=north", "half=lower", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3824, "facing=north", "half=lower", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3825, "facing=south", "half=upper", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3826, "facing=south", "half=upper", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3827, "facing=south", "half=upper", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3828, "facing=south", "half=upper", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3829, "facing=south", "half=upper", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3830, "facing=south", "half=upper", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3831, "facing=south", "half=upper", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3832, "facing=south", "half=upper", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3833, "facing=south", "half=lower", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3834, "facing=south", "half=lower", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3835, "facing=south", "half=lower", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3836, "facing=south", "half=lower", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3837, "facing=south", "half=lower", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3838, "facing=south", "half=lower", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3839, "facing=south", "half=lower", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3840, "facing=south", "half=lower", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3841, "facing=west", "half=upper", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3842, "facing=west", "half=upper", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3843, "facing=west", "half=upper", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3844, "facing=west", "half=upper", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3845, "facing=west", "half=upper", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3846, "facing=west", "half=upper", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3847, "facing=west", "half=upper", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3848, "facing=west", "half=upper", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3849, "facing=west", "half=lower", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3850, "facing=west", "half=lower", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3851, "facing=west", "half=lower", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3852, "facing=west", "half=lower", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3853, "facing=west", "half=lower", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3854, "facing=west", "half=lower", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3855, "facing=west", "half=lower", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3856, "facing=west", "half=lower", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3857, "facing=east", "half=upper", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3858, "facing=east", "half=upper", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3859, "facing=east", "half=upper", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3860, "facing=east", "half=upper", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3861, "facing=east", "half=upper", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3862, "facing=east", "half=upper", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3863, "facing=east", "half=upper", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3864, "facing=east", "half=upper", "hinge=right", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3865, "facing=east", "half=lower", "hinge=left", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3866, "facing=east", "half=lower", "hinge=left", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3867, "facing=east", "half=lower", "hinge=left", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3868, "facing=east", "half=lower", "hinge=left", "open=false", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3869, "facing=east", "half=lower", "hinge=right", "open=true", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3870, "facing=east", "half=lower", "hinge=right", "open=true", "powered=false"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3871, "facing=east", "half=lower", "hinge=right", "open=false", "powered=true"));
		IRON_DOOR.addBlockAlternative(new BlockAlternative((short) 3872, "facing=east", "half=lower", "hinge=right", "open=false", "powered=false"));
	}
}
