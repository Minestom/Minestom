package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Beehive {
	public static void initStates() {
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15808, "facing=north", "honey_level=0"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15809, "facing=north", "honey_level=1"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15810, "facing=north", "honey_level=2"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15811, "facing=north", "honey_level=3"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15812, "facing=north", "honey_level=4"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15813, "facing=north", "honey_level=5"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15814, "facing=south", "honey_level=0"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15815, "facing=south", "honey_level=1"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15816, "facing=south", "honey_level=2"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15817, "facing=south", "honey_level=3"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15818, "facing=south", "honey_level=4"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15819, "facing=south", "honey_level=5"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15820, "facing=west", "honey_level=0"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15821, "facing=west", "honey_level=1"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15822, "facing=west", "honey_level=2"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15823, "facing=west", "honey_level=3"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15824, "facing=west", "honey_level=4"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15825, "facing=west", "honey_level=5"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15826, "facing=east", "honey_level=0"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15827, "facing=east", "honey_level=1"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15828, "facing=east", "honey_level=2"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15829, "facing=east", "honey_level=3"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15830, "facing=east", "honey_level=4"));
		BEEHIVE.addBlockAlternative(new BlockAlternative((short) 15831, "facing=east", "honey_level=5"));
	}
}
