package net.minestom.server.item;

import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Material extends ProtocolObject, Materials permits MaterialImpl {

    /**
     * Returns the material registry.
     *
     * @return the material registry
     */
    @Contract(pure = true)
    @NotNull Registry.MaterialEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default int maxStackSize() {
        return registry().maxStackSize();
    }

    default boolean isFood() {
        return registry().isFood();
    }

    default boolean isBlock() {
        return registry().block() != null;
    }

    default Block block() {
        return registry().block();
    }

    default boolean isArmor() {
        return registry().isArmor();
    }

    default boolean hasState() {
        if (this == BOW || this == TRIDENT || this == CROSSBOW || this == SHIELD) {
            return true;
        } else {
            return isFood();
        }
    }

    static @NotNull Collection<@NotNull Material> values() {
        return MaterialImpl.values();
    }

    static @Nullable Material fromNamespaceId(@NotNull String namespaceID) {
        return MaterialImpl.getSafe(namespaceID);
    }

    static @Nullable Material fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Material fromId(int id) {
        return MaterialImpl.getId(id);
    }
}
