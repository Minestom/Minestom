package net.minestom.server.instance.block.incubator;

public class BlockProperties {
    public static final BlockProperty<String> FACING = new BlockProperty<>("facing", "north", "south", "west", "east");

    public static final BlockProperty<String> HALF = new BlockProperty<>("half", "top", "bottom");

    public static final BlockProperty<String> SHAPE = new BlockProperty<>("shape", "straight", "inner_left", "inner_right", "outer_left", "outer_right");

    public static final BlockProperty<Boolean> WATER_LOGGED = new BlockProperty<>("waterlogged", true, false);
}
