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
import java.util.Map;

public interface TrimMaterial extends ProtocolObject {
    static @NotNull TrimMaterial create(@NotNull NamespaceID namespace,
                                        @NotNull String assetName,
                                        @NotNull Material ingredient,
                                        float itemModelIndex,
                                        @NotNull Map<String, String> overrideArmorMaterials,
                                        @NotNull Component description,
                                        Registry.Properties custom) {
        return new TrimMaterialImpl(
                new Registry.TrimMaterialEntry(
                        namespace,
                        assetName,
                        ingredient,
                        itemModelIndex,
                        overrideArmorMaterials,
                        description,
                        custom
                )
        );
    }

    static @NotNull TrimMaterial create(@NotNull NamespaceID namespace,
                                        @NotNull String assetName,
                                        @NotNull Material ingredient,
                                        float itemModelIndex,
                                        @NotNull Map<String, String> overrideArmorMaterials,
                                        @NotNull Component description) {
        return new TrimMaterialImpl(
                new Registry.TrimMaterialEntry(
                        namespace,
                        assetName,
                        ingredient,
                        itemModelIndex,
                        overrideArmorMaterials,
                        description,
                        null
                )
        );
    }

    static Collection<TrimMaterial> values() {
        return TrimMaterialImpl.values();
    }

    @Contract(pure = true)
    @NotNull Registry.TrimMaterialEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    default @NotNull String assetName() {
        return registry().assetName();
    }

    default @NotNull Material ingredient() {
        return registry().ingredient();
    }

    default float itemModelIndex() {
        return registry().itemModelIndex();
    }

    default @NotNull Map<String, String> overrideArmorMaterials() {
        return registry().overrideArmorMaterials();
    }

    default @NotNull Component description() {
        return registry().description();
    }

    NBTCompound asNBT();

}
