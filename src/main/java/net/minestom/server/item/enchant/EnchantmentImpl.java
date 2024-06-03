package net.minestom.server.item.enchant;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

record EnchantmentImpl(
        @NotNull NamespaceID namespace,
        @NotNull Component description,
        @NotNull Set<NamespaceID> exclusiveSet,
        @NotNull Set<NamespaceID> supportedItems,
        @NotNull Set<NamespaceID> primaryItems,
        int weight,
        int maxLevel,
        // min/max cost
        int anvilCost,
        @NotNull List<AttributeSlot> slots,
        @Nullable Registry.EnchantmentEntry registry
) implements Enchantment {

    static final BinaryTagSerializer<Enchantment> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("BannerPattern is read-only");
            },
            bannerPattern -> {
                throw new UnsupportedOperationException("todo");
            }
    );

    EnchantmentImpl {
        Check.notNull(namespace, "Namespace cannot be null");
    }

    EnchantmentImpl(@NotNull Registry.EnchantmentEntry registry) {
        //todo sets
        this(registry.namespace(), registry.description(), Set.of(), registry);
    }

}
