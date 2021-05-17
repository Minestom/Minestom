package net.minestom.server.instance.block.incubator;

import net.minestom.server.utils.NamespaceID;

import java.util.Collections;
import java.util.List;

public class Blocks {

    public static final BlockType STONE = BlockImpl.create(NamespaceID.from("minecraft:stone"), (short) 1, (short) 1, List.of(BlockProperties.BED_PART));

}
