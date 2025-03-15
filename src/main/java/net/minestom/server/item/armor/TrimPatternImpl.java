package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record TrimPatternImpl(
        @NotNull Key assetId,
        @NotNull Component description,
        boolean isDecal,
        @Nullable Registry.TrimPatternEntry registry
) implements TrimPattern {

    static final BinaryTagSerializer<TrimPattern> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("TrimMaterial is read-only");
            },
            trimPattern -> CompoundBinaryTag.builder()
                    .putString("asset_id", trimPattern.assetId().asString())
                    .put("description", BinaryTagSerializer.NBT_COMPONENT.write(trimPattern.description()))
                    .putBoolean("decal", trimPattern.isDecal())
                    .build()
    );

    TrimPatternImpl {
        Check.notNull(assetId, "missing asset id");
        Check.notNull(description, "missing description");
    }

    TrimPatternImpl(@NotNull Registry.TrimPatternEntry registry) {
        this(registry.assetID(), registry.description(), registry.decal(), registry);
    }
}
