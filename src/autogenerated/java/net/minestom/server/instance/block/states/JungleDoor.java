package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class JungleDoor {
	public static void initStates() {
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8330, "facing=north", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8331, "facing=north", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8332, "facing=north", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8333, "facing=north", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8334, "facing=north", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8335, "facing=north", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8336, "facing=north", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8337, "facing=north", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8338, "facing=north", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8339, "facing=north", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8340, "facing=north", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8341, "facing=north", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8342, "facing=north", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8343, "facing=north", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8344, "facing=north", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8345, "facing=north", "half=lower", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8346, "facing=south", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8347, "facing=south", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8348, "facing=south", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8349, "facing=south", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8350, "facing=south", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8351, "facing=south", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8352, "facing=south", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8353, "facing=south", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8354, "facing=south", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8355, "facing=south", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8356, "facing=south", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8357, "facing=south", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8358, "facing=south", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8359, "facing=south", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8360, "facing=south", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8361, "facing=south", "half=lower", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8362, "facing=west", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8363, "facing=west", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8364, "facing=west", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8365, "facing=west", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8366, "facing=west", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8367, "facing=west", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8368, "facing=west", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8369, "facing=west", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8370, "facing=west", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8371, "facing=west", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8372, "facing=west", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8373, "facing=west", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8374, "facing=west", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8375, "facing=west", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8376, "facing=west", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8377, "facing=west", "half=lower", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8378, "facing=east", "half=upper", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8379, "facing=east", "half=upper", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8380, "facing=east", "half=upper", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8381, "facing=east", "half=upper", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8382, "facing=east", "half=upper", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8383, "facing=east", "half=upper", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8384, "facing=east", "half=upper", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8385, "facing=east", "half=upper", "hinge=right", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8386, "facing=east", "half=lower", "hinge=left", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8387, "facing=east", "half=lower", "hinge=left", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8388, "facing=east", "half=lower", "hinge=left", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8389, "facing=east", "half=lower", "hinge=left", "open=false", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8390, "facing=east", "half=lower", "hinge=right", "open=true", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8391, "facing=east", "half=lower", "hinge=right", "open=true", "powered=false"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8392, "facing=east", "half=lower", "hinge=right", "open=false", "powered=true"));
		JUNGLE_DOOR.addBlockAlternative(new BlockAlternative((short) 8393, "facing=east", "half=lower", "hinge=right", "open=false", "powered=false"));
	}
}
