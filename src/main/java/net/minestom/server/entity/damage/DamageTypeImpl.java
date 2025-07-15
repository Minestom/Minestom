package net.minestom.server.entity.damage;

import net.minestom.server.utils.validate.Check;
import org.jspecify.annotations.Nullable;

record DamageTypeImpl(
        String messageId,
        String scaling,
        float exhaustion,
        @Nullable String effects,
        @Nullable String deathMessageType
) implements DamageType {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    DamageTypeImpl {
        Check.argCondition(messageId == null || messageId.isEmpty(), "missing message id");
        Check.argCondition(scaling == null || scaling.isEmpty(), "missing scaling");
    }

}