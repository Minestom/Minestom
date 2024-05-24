package net.minestom.server.attribute;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Attribute extends ProtocolObject permits AttributeImpl {
    @Contract(pure = true)
    @Nullable
    Registry.AttributeEntry registry();

    @Override
    @NotNull
    NamespaceID namespace();

    float defaultValue();
    float maxValue();
    float mineValue();
    boolean clientSync();
    @NotNull
    String translationKey();
}
