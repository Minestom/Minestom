package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Bell {
	public static void initStates() {
		BELL.addBlockAlternative(new BlockAlternative((short) 14858, "attachment=floor", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14859, "attachment=floor", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14860, "attachment=floor", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14861, "attachment=floor", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14862, "attachment=floor", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14863, "attachment=floor", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14864, "attachment=floor", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14865, "attachment=floor", "facing=east", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14866, "attachment=ceiling", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14867, "attachment=ceiling", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14868, "attachment=ceiling", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14869, "attachment=ceiling", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14870, "attachment=ceiling", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14871, "attachment=ceiling", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14872, "attachment=ceiling", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14873, "attachment=ceiling", "facing=east", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14874, "attachment=single_wall", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14875, "attachment=single_wall", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14876, "attachment=single_wall", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14877, "attachment=single_wall", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14878, "attachment=single_wall", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14879, "attachment=single_wall", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14880, "attachment=single_wall", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14881, "attachment=single_wall", "facing=east", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14882, "attachment=double_wall", "facing=north", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14883, "attachment=double_wall", "facing=north", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14884, "attachment=double_wall", "facing=south", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14885, "attachment=double_wall", "facing=south", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14886, "attachment=double_wall", "facing=west", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14887, "attachment=double_wall", "facing=west", "powered=false"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14888, "attachment=double_wall", "facing=east", "powered=true"));
		BELL.addBlockAlternative(new BlockAlternative((short) 14889, "attachment=double_wall", "facing=east", "powered=false"));
	}
}
