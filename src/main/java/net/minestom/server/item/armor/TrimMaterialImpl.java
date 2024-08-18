package net.minestom.server.item.armor;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

record TrimMaterialImpl(
        @NotNull String assetName,
        @NotNull Material ingredient,
        float itemModelIndex,
        @NotNull Map<String, String> overrideArmorMaterials,
        @NotNull Component description,
        @Nullable Registry.TrimMaterialEntry registry
) implements TrimMaterial {

    static final BinaryTagSerializer<TrimMaterial> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("TrimMaterial is read-only");
            },
            trimMaterial -> {
                CompoundBinaryTag.Builder overrideArmorMaterials = CompoundBinaryTag.builder();
                for (Map.Entry<String, String> entry : trimMaterial.overrideArmorMaterials().entrySet()) {
                    overrideArmorMaterials.putString(entry.getKey(), entry.getValue());
                }

                return CompoundBinaryTag.builder()
                        .putString("asset_name", trimMaterial.assetName())
                        .put("ingredient", Material.NBT_TYPE.write(trimMaterial.ingredient()))
                        .putFloat("item_model_index", trimMaterial.itemModelIndex())
                        .put("override_armor_materials", overrideArmorMaterials.build())
                        .put("description", BinaryTagSerializer.NBT_COMPONENT.write(trimMaterial.description()))
                        .build();
            }
    );

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    TrimMaterialImpl {
        Check.argCondition(assetName == null || assetName.isEmpty(), "missing asset name");
        Check.argCondition(ingredient == null, "missing ingredient");
        Check.argCondition(overrideArmorMaterials == null, "missing override armor materials");
        Check.argCondition(description == null, "missing description");
        overrideArmorMaterials = Map.copyOf(overrideArmorMaterials);
    }

    TrimMaterialImpl(@NotNull Registry.TrimMaterialEntry registry) {
        this(registry.assetName(), registry.ingredient(),
                registry.itemModelIndex(), registry.overrideArmorMaterials(),
                registry.description(), registry);
    }

}
