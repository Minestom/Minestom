package net.minestom.server.entity.damage;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record DamageTypeImpl(
        NamespaceID namespace,
        float exhaustion,
        @NotNull String messageId,
        @NotNull String scaling,
        @Nullable Registry.DamageTypeEntry registry
) implements DamageType {

    static final BinaryTagSerializer<DamageType> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("DamageType is read-only");
            },
            damageType -> CompoundBinaryTag.builder()
                    .putFloat("exhaustion", damageType.exhaustion())
                    .putString("message_id", damageType.messageId())
                    .putString("scaling", damageType.scaling())
                    .build()
    );

    // The builder can violate the nullability constraints
    @SuppressWarnings("ConstantValue")
    DamageTypeImpl {
        Check.argCondition(messageId == null || messageId.isEmpty(), "missing message id");
        Check.argCondition(scaling == null || scaling.isEmpty(), "missing scaling");
    }

    DamageTypeImpl(@NotNull Registry.DamageTypeEntry registry) {
        this(registry.namespace(), registry.exhaustion(), registry.messageId(), registry.scaling(), registry);
    }
}