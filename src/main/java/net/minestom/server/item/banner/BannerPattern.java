package net.minestom.server.item.banner;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

// Microtus -  Banner and Shield Meta
public sealed interface BannerPattern extends ProtocolObject, BannerPatterns permits BannerPatternImpl {
    static @NotNull Collection<@NotNull BannerPattern> values() {
        return BannerPatternImpl.values();
    }

    static @Nullable BannerPattern fromNamespaceId(@NotNull String namespaceID) {
        return BannerPatternImpl.getSafe(namespaceID);
    }

    static @Nullable BannerPattern fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable BannerPattern fromId(int id) {
        return BannerPatternImpl.getId(id);
    }

    static @Nullable BannerPattern fromIdentifier(String identifier) {
        return BannerPatternImpl.getIdentifier(identifier);
    }

    @Override
    default @NotNull Key key() {
        return ProtocolObject.super.key();
    }

    @NotNull String identifier();
}
