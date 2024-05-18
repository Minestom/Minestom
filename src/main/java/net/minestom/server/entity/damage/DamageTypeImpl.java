package net.minestom.server.entity.damage;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record DamageTypeImpl(
        @NotNull NamespaceID namespace,
        float exhaustion,
        @NotNull String messageId,
        @NotNull String scaling,
        @Nullable Registry.DamageTypeEntry registry
) implements DamageType {

    static final BinaryTagSerializer<DamageType> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("DamageType is read-only");
            },
            damageType -> CompoundBinaryTag.builder()
                    .putFloat("exhaustion", damageType.exhaustion())
                    .putString("message_id", damageType.messageId())
                    .putString("scaling", damageType.scaling())
                    .build()
    );

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    DamageTypeImpl {
        Check.notNull(namespace, "Namespace cannot be null");
        Check.argCondition(messageId == null || messageId.isEmpty(), "missing message id: {0}", namespace);
        Check.argCondition(scaling == null || scaling.isEmpty(), "missing scaling: {0}", namespace);
    }

    DamageTypeImpl(@NotNull Registry.DamageTypeEntry registry) {
        this(registry.namespace(), registry.exhaustion(), registry.messageId(), registry.scaling(), registry);
    }

}