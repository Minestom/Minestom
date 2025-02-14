package net.minestom.server.entity.attribute;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record AttributeImpl(
        double defaultValue,
        boolean isSynced,
        double maxValue,
        double minValue,
        @Nullable Registry.AttributeEntry registry) implements Attribute {
    static final BinaryTagSerializer<Attribute> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("Attribute is read-only");
            },
            attribute -> CompoundBinaryTag.builder()
                    .putDouble("defaultValue", attribute.defaultValue())
                    .putBoolean("clientSync", attribute.isSynced())
                    .putDouble("maxValue", attribute.maxValue())
                    .putDouble("minValue", attribute.minValue())
                    .build()
    );

    AttributeImpl(@NotNull Registry.AttributeEntry registry) {
        this(registry.defaultValue(), registry.clientSync(), registry.maxValue(), registry.minValue(), registry);
    }
}
