package net.minestom.server.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.fluid.Fluid;
import org.jetbrains.annotations.NotNull;

public final class FluidGameTags {

    public static final @NotNull GameTag<@NotNull Fluid> WATER = get("water");
    public static final @NotNull GameTag<@NotNull Fluid> LAVA = get("lava");

    private static GameTag<Fluid> get(final String name) {
        return MinecraftServer.getTagManager().get(GameTagType.FLUIDS, "minecraft:" + name);
    }

    private FluidGameTags() {
    }
}
