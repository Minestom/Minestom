package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Bell {
	public static void initStates() {
		BELL.addBlockAlternative(new BlockAlternative((short) 11198, "attachment=floor", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11199, "attachment=floor", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11200, "attachment=floor", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11201, "attachment=floor", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11202, "attachment=floor", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11203, "attachment=floor", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11204, "attachment=floor", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11205, "attachment=floor", "facing=east", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11206, "attachment=ceiling", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11207, "attachment=ceiling", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11208, "attachment=ceiling", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11209, "attachment=ceiling", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11210, "attachment=ceiling", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11211, "attachment=ceiling", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11212, "attachment=ceiling", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11213, "attachment=ceiling", "facing=east", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11214, "attachment=single_wall", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11215, "attachment=single_wall", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11216, "attachment=single_wall", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11217, "attachment=single_wall", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11218, "attachment=single_wall", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11219, "attachment=single_wall", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11220, "attachment=single_wall", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11221, "attachment=single_wall", "facing=east", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11222, "attachment=double_wall", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11223, "attachment=double_wall", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11224, "attachment=double_wall", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11225, "attachment=double_wall", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11226, "attachment=double_wall", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11227, "attachment=double_wall", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11228, "attachment=double_wall", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 11229, "attachment=double_wall", "facing=east", "powered=false"));
	}
}
