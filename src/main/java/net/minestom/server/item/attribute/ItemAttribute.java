package net.minestom.server.item.attribute;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ItemAttribute(@NotNull UUID uuid,
                            @NotNull String name,
                            @NotNull Attribute attribute,
                            @NotNull AttributeOperation operation, double amount,
                            @NotNull AttributeSlot slot) {
}
