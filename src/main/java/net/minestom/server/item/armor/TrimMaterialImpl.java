package net.minestom.server.item.armor;

import net.kyori.adventure.text.Component;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

record TrimMaterialImpl(
        @NotNull String assetName,
        @NotNull Map<String, String> overrideArmorMaterials,
        @NotNull Component description
) implements TrimMaterial {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    TrimMaterialImpl {
        Check.argCondition(assetName == null || assetName.isEmpty(), "missing asset name");
        Check.argCondition(overrideArmorMaterials == null, "missing override armor materials");
        Check.argCondition(description == null, "missing description");
        overrideArmorMaterials = Map.copyOf(overrideArmorMaterials);
    }

}
