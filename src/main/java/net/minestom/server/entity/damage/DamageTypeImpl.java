package net.minestom.server.entity.damage;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

record DamageTypeImpl(
        float exhaustion,
        @NotNull String messageId,
        @NotNull String scaling
) implements DamageType {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    DamageTypeImpl {
        Check.argCondition(messageId == null || messageId.isEmpty(), "missing message id");
        Check.argCondition(scaling == null || scaling.isEmpty(), "missing scaling");
    }

}