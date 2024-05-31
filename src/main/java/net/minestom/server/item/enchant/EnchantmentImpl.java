package net.minestom.server.item.enchant;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record EnchantmentImpl(@NotNull NamespaceID namespace, @Nullable Registry.EnchantmentEntry registry) implements Enchantment {

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
        this(registry.namespace(), registry);
    }

}
