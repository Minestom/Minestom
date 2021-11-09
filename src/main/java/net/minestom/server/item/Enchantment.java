package net.minestom.server.item;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Enchantment extends ProtocolObject, Enchantments permits EnchantmentImpl {

    /**
     * Returns the enchantment registry.
     *
     * @return the enchantment registry
     */
    @Contract(pure = true)
    @NotNull Registry.EnchantmentEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    static @NotNull Collection<@NotNull Enchantment> values() {
        return EnchantmentImpl.values();
    }

    static @Nullable Enchantment fromNamespaceId(@NotNull String namespaceID) {
        return EnchantmentImpl.getSafe(namespaceID);
    }

    static @Nullable Enchantment fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Enchantment fromId(int id) {
        return EnchantmentImpl.getId(id);
    }
}
