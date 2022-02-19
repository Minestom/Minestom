package net.minestom.server.map;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface MapColor extends MapColors, ProtocolObject, RGBLike permits MapColorImpl {
    static @NotNull Collection<@NotNull MapColor> values() {
        return MapColorImpl.values();
    }

    static @Nullable MapColor fromNamespaceId(@NotNull String namespaceID) {
        return MapColorImpl.getSafe(namespaceID);
    }

    static @Nullable MapColor fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable MapColor fromId(int id) {
        return MapColorImpl.getId(id);
    }

    @NotNull
    static PreciseMapColor closestColor(int argb) {
        return PreciseMapColor.closestColor(argb);
    }
    @NotNull
    static PreciseMapColor closestColor(int argb, ColorMappingStrategy strategy) {
        return PreciseMapColor.closestColor(argb, strategy);
    }

    /**
     * Returns the material registry.
     *
     * @return the material registry
     */
    @Contract(pure = true)
    @NotNull Registry.MapColorEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default int red() {
        return registry().red();
    }

    default int green() {
        return registry().green();
    }

    default int blue() {
        return registry().blue();
    }

    // From the wiki: https://minecraft.gamepedia.com/Map_item_format
    // Map Color ID 	Multiply R,G,B By 	= Multiplier
    //Base Color ID*4 + 0 	180 	0.71
    //Base Color ID*4 + 1 	220 	0.86
    //Base Color ID*4 + 2 	255 (same color) 	1
    //Base Color ID*4 + 3 	135 	0.53

    /**
     * Returns the color index with RGB multiplied by 0.53, to use on a map
     */
    default byte multiply53() {
        return (byte) ((id() << 2) + 3);
    }

    /**
     * Returns the color index with RGB multiplied by 0.86, to use on a map
     */
    default byte multiply86() {
        return (byte) ((id() << 2) + 1);
    }

    /**
     * Returns the color index with RGB multiplied by 0.71, to use on a map
     */
    default byte multiply71() {
        return (byte) (id() << 2);
    }

    /**
     * Returns the color index to use on a map
     */
    default byte baseColor() {
        return (byte) ((id() << 2) + 2);
    }
}
