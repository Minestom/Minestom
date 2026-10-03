package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.validate.Check;

record TrimMaterialImpl(
        Key paletteId,
        Component description
) implements TrimMaterial {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    TrimMaterialImpl {
        Check.argCondition(paletteId == null, "missing palette id");
        Check.argCondition(description == null, "missing description");
    }

}
