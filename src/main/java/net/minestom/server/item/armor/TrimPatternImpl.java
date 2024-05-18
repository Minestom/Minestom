package net.minestom.server.item.armor;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record TrimPatternImpl(
        @NotNull NamespaceID namespace,
        @NotNull NamespaceID assetId,
        @NotNull Material template,
        @NotNull Component description,
        boolean isDecal,
        @Nullable Registry.TrimPatternEntry registry
) implements TrimPattern {

    static final BinaryTagSerializer<TrimPattern> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("TrimMaterial is read-only");
            },
            trimPattern -> CompoundBinaryTag.builder()
                    .putString("asset_id", trimPattern.assetId().asString())
                    .put("template_item", Material.NBT_TYPE.write(trimPattern.template()))
                    .put("description", BinaryTagSerializer.NBT_COMPONENT.write(trimPattern.description()))
                    .putBoolean("decal", trimPattern.isDecal())
                    .build()
    );

    TrimPatternImpl {
        Check.notNull(namespace, "Namespace cannot be null");
        Check.notNull(assetId, "missing asset id: {0}", namespace);
        Check.notNull(template, "missing template: {0}", namespace);
        Check.notNull(description, "missing description: {0}", namespace);
    }

    TrimPatternImpl(@NotNull Registry.TrimPatternEntry registry) {
        this(registry.namespace(), registry.assetID(), registry.template(),
                registry.description(), registry.decal(), registry);
    }
}
