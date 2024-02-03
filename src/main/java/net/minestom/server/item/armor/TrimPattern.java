package net.minestom.server.item.armor;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collection;

public interface TrimPattern extends ProtocolObject {
    static @NotNull TrimPattern create(@NotNull NamespaceID namespace,
                                       @NotNull NamespaceID assetID,
                                       @NotNull Material template,
                                       @NotNull Component description,
                                       boolean decal,
                                       @NotNull Registry.Properties custom) {
        return new TrimPatternImpl(
                new Registry.TrimPatternEntry(namespace, assetID, template, description, decal, custom)
        );
    }

    static @NotNull TrimPattern create(@NotNull NamespaceID namespace,
                                       @NotNull NamespaceID assetID,
                                       @NotNull Material template,
                                       @NotNull Component description,
                                       boolean decal) {
        return new TrimPatternImpl(
                new Registry.TrimPatternEntry(namespace, assetID, template, description, decal, null)
        );
    }

    static Collection<TrimPattern> values() {
        return TrimPatternImpl.values();
    }

    @Contract(pure = true)
    @NotNull Registry.TrimPatternEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    default @NotNull NamespaceID assetID() {
        return registry().assetID();
    }

    default @NotNull Material template() {
        return registry().template();
    }

    default @NotNull Component description() {
        return registry().description();
    }

    default boolean decal() {
        return registry().decal();
    }

    NBTCompound asNBT();

}
