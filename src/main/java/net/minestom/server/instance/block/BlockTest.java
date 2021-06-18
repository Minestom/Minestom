package net.minestom.server.instance.block;

import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class BlockTest implements Block {

    private final Registry.BlockEntry registry;

    private final Map<String, String> properties;
    private final NBTCompound compound;
    private final BlockHandler handler;

    private final Map<String, String> unmodifiableProperties;

    BlockTest(Registry.BlockEntry registry,
              Map<String, String> properties,
              NBTCompound compound,
              BlockHandler handler) {
        this.registry = registry;
        this.properties = properties;
        this.compound = compound;
        this.handler = handler;

        this.unmodifiableProperties = properties != null ?
                Collections.unmodifiableMap(properties) : null;
    }

    BlockTest(Registry.BlockEntry registry,
              Map<String, String> properties) {
        this(registry, properties, null, null);
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        var properties = new HashMap<>(this.properties);
        properties.put(property, value);
        final Block block = BlockRegistry.getProperties(this, properties);
        return Objects.requireNonNull(block);
    }

    @Override
    public @NotNull Block withNbt(@Nullable NBTCompound compound) {
        return new BlockTest(registry, properties, compound, handler);
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockTest(registry, properties, compound, handler);
    }

    @Override
    public @Nullable NBTCompound getNbt() {
        return compound != null ? compound.deepClone() : null;
    }

    @Override
    public @Nullable BlockHandler getHandler() {
        return handler;
    }

    @Override
    public @NotNull Map<String, String> getProperties() {
        return unmodifiableProperties;
    }

    @Override
    public @NotNull Registry.BlockEntry registry() {
        return registry;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(compound);
    }
}
