package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionType extends StaticProtocolObject, PotionTypes permits PotionTypeImpl {

    static @NotNull Collection<@NotNull PotionType> values() {
        return PotionTypeImpl.values();
    }

    static @Nullable PotionType fromKey(@NotNull String key) {
        return PotionTypeImpl.getSafe(key);
    }

    static @Nullable PotionType fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    /**
     * @deprecated use {@link #fromKey(String)}
     */
    @Deprecated
    static PotionType fromNamespaceId(@NotNull String namespaceID) {
        return fromKey(namespaceID);
    }

    /**
     * @deprecated use {@link #fromKey(Key)}
     */
    @Deprecated
    static PotionType fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromKey(namespaceID);
    }


    static @Nullable PotionType fromId(int id) {
        return PotionTypeImpl.getId(id);
    }
}
