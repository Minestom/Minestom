package net.minestom.server.instance.block;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class BlockImpl implements Block {
    private static final Cache<NBTCompound, NBTCompound> NBT_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .weakValues()
            .build();

    private final Registry.BlockEntry registry;
    private final BlockLoader.PropertyEntry propertyEntry;
    private final Map<String, String> properties;
    private final NBTCompound nbt;
    private final BlockHandler handler;

    BlockImpl(@NotNull Registry.BlockEntry registry,
              @NotNull BlockLoader.PropertyEntry propertyEntry,
              @NotNull Map<String, String> properties,
              @Nullable NBTCompound nbt,
              @Nullable BlockHandler handler) {
        this.registry = registry;
        this.propertyEntry = propertyEntry;
        this.properties = properties;
        this.nbt = nbt;
        this.handler = handler;
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        var properties = new HashMap<>(this.properties);
        properties.replace(property, value);
        return compute(properties);
    }

    @Override
    public @NotNull Block withProperties(@NotNull Map<@NotNull String, @NotNull String> properties) {
        if (properties.isEmpty()) {
            return this;
        }
        if (this.properties.size() == properties.size()) {
            return compute(properties); // Map should be complete
        }
        var newProperties = new HashMap<>(this.properties);
        newProperties.replaceAll((key, value) -> Objects.requireNonNullElse(properties.get(key), value));
        return compute(newProperties);
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        var compound = Objects.requireNonNullElseGet(nbt(), NBTCompound::new);
        tag.write(compound, value);
        final var nbt = compound.getSize() > 0 ? NBT_CACHE.get(compound, c -> compound) : null;
        return new BlockImpl(registry, propertyEntry, properties, nbt, handler);
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, propertyEntry, properties, nbt, handler);
    }

    @Override
    public boolean hasNbt() {
        return nbt != null;
    }

    @Override
    public @Nullable BlockHandler handler() {
        return handler;
    }

    @Override
    public @NotNull Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public @NotNull Registry.BlockEntry registry() {
        return registry;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return nbt != null ? tag.read(nbt) : null;
    }

    private Block compute(Map<String, String> properties) {
        Block block = propertyEntry.getProperties(properties);
        if (block == null)
            throw new IllegalArgumentException("Invalid properties: " + properties);
        return nbt == null && handler == null ? block :
                new BlockImpl(block.registry(), propertyEntry, block.properties(), nbt, handler);
    }
}
