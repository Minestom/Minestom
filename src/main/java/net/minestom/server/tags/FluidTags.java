package net.minestom.server.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.fluid.Fluid;
import org.jetbrains.annotations.NotNull;

public final class FluidTags {

    public static final @NotNull Tag<@NotNull Fluid> WATER = get("water");
    public static final @NotNull Tag<@NotNull Fluid> LAVA = get("lava");

    private static Tag<Fluid> get(final String name) {
        return MinecraftServer.getTagManager().get(TagType.FLUIDS, "minecraft:" + name);
    }

    private FluidTags() {
    }
}
