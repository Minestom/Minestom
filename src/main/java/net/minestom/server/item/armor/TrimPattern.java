package net.minestom.server.item.armor;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.DynamicRegistryImpl;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface TrimPattern extends ProtocolObject permits TrimPatternImpl {
    @NotNull BinaryTagSerializer<TrimPattern> NBT_TYPE = TrimPatternImpl.NBT_TYPE;

    static @NotNull TrimPattern create(
            @NotNull NamespaceID namespace,
            @NotNull NamespaceID assetId,
            @NotNull Material template,
            @NotNull Component description,
            boolean decal
    ) {
        return new TrimPatternImpl(namespace, assetId, template, description, decal, null);
    }

    static @NotNull Builder builder(@NotNull String namespace) {
        return builder(NamespaceID.from(namespace));
    }

    static @NotNull Builder builder(@NotNull NamespaceID namespace) {
        return new Builder(namespace);
    }

    /**
     * <p>Creates a new registry for trim materials, loading the vanilla trim materials.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<TrimPattern> createDefaultRegistry() {
        return new DynamicRegistryImpl<>(
                "minecraft:trim_pattern", NBT_TYPE, Registry.Resource.TRIM_PATTERNS,
                (namespace, props) -> new TrimPatternImpl(Registry.trimPattern(namespace, props))
        );
    }

    @NotNull NamespaceID assetId();

    @NotNull Material template();

    @NotNull Component description();

    boolean isDecal();

    @Contract(pure = true)
    @Nullable Registry.TrimPatternEntry registry();

    final class Builder {
        private final NamespaceID namespace;
        private NamespaceID assetId;
        private Material template;
        private Component description;
        private boolean decal;

        Builder(@NotNull NamespaceID namespace) {
            this.namespace = namespace;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder assetId(@NotNull String assetId) {
            return assetId(NamespaceID.from(assetId));
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder assetId(@NotNull NamespaceID assetId) {
            this.assetId = assetId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder template(@NotNull Material template) {
            this.template = template;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder decal(boolean decal) {
            this.decal = decal;
            return this;
        }

        @Contract(pure = true)
        public @NotNull TrimPattern build() {
            return new TrimPatternImpl(namespace, assetId, template, description, decal, null);
        }
    }
}
