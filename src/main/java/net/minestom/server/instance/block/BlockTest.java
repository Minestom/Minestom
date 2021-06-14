package net.minestom.server.instance.block;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class BlockTest implements Block {

    private final Registry.BlockEntry registry;

    private final Map<String, String> properties = new HashMap<>();
    private NBTCompound compound;
    private BlockHandler handler;

    BlockTest(Registry.BlockEntry registry) {
        this.registry = registry;
    }

    BlockTest(JsonObject jsonObject) {
        this(Registry.block(jsonObject));
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        var properties = new HashMap<>(this.properties);
        properties.put(property, value);
        return Objects.requireNonNull(BlockRegistry.getProperties(getNamespaceId().asString(), properties));
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        var clone = shallowClone();
        clone.compound = Objects.requireNonNullElseGet(clone.compound, NBTCompound::new);
        tag.write(clone.compound, value);
        return clone;
    }

    @Override
    public @NotNull Block withNbt(@Nullable NBTCompound compound) {
        var clone = shallowClone();
        clone.compound = compound;
        return clone;
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        var clone = shallowClone();
        clone.handler = handler;
        return clone;
    }

    @Override
    public @NotNull String getProperty(@NotNull String property) {
        return properties.get(property);
    }

    @Override
    public @Nullable NBTCompound getNbt() {
        return compound.deepClone();
    }

    @Override
    public @Nullable BlockHandler getHandler() {
        return handler;
    }

    @Override
    public @NotNull Map<String, String> createPropertiesMap() {
        return new HashMap<>(properties);
    }

    @Override
    public @NotNull Registry.BlockEntry registry() {
        return registry;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(compound);
    }

    @Override
    public boolean hasTag(@NotNull Tag<?> tag) {
        return compound.containsKey(tag.getKey());
    }

    private @NotNull BlockTest shallowClone() {
        return new BlockTest(registry);
    }
}
