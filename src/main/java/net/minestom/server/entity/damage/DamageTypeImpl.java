package net.minestom.server.entity.damage;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record DamageTypeImpl(
        float exhaustion,
        @NotNull String messageId,
        @NotNull String scaling,
        @Nullable Registry.DamageTypeEntry registry
) implements DamageType {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    DamageTypeImpl {
        Check.argCondition(messageId == null || messageId.isEmpty(), "missing message id");
        Check.argCondition(scaling == null || scaling.isEmpty(), "missing scaling");
    }

    DamageTypeImpl(@NotNull Registry.DamageTypeEntry registry) {
        this(registry.exhaustion(), registry.messageId(), registry.scaling(), registry);
    }

}