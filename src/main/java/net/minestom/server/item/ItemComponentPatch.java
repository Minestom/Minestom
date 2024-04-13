package net.minestom.server.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * <p>Holds the altered components of an itemstack.</p>
 *
 * <p>The inner map contains the value for added components, null for removed components, and no entry for unmodified components.</p>
 *
 * @param patch
 */
record ItemComponentPatch(@NotNull Int2ObjectMap<Object> patch) {
    private static final char REMOVAL_PREFIX = '!';

    public static final ItemComponentPatch EMPTY = new ItemComponentPatch(new Int2ObjectArrayMap<>(0));

    public static final @NotNull NetworkBuffer.Type<ItemComponentPatch> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ItemComponentPatch value) {
            int added = 0;
            for (Object o : value.patch.values()) {
                if (o != null) added++;
            }

            buffer.write(NetworkBuffer.VAR_INT, added);
            buffer.write(NetworkBuffer.VAR_INT, value.patch.size() - added);
            for (Int2ObjectMap.Entry<Object> entry : value.patch.int2ObjectEntrySet()) {
                if (entry.getValue() != null) {
                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                    //noinspection unchecked
                    ItemComponentType<Object> type = (ItemComponentType<Object>) ItemComponentType.fromId(entry.getIntKey());
                    assert type != null;
                    type.write(entry.getValue());
                }
            }
            for (Int2ObjectMap.Entry<Object> entry : value.patch.int2ObjectEntrySet()) {
                if (entry.getValue() == null) {
                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                }
            }
        }

        @Override
        public ItemComponentPatch read(@NotNull NetworkBuffer buffer) {
            int added = buffer.read(NetworkBuffer.VAR_INT);
            int removed = buffer.read(NetworkBuffer.VAR_INT);
            Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(added + removed);
            for (int i = 0; i < added; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                //noinspection unchecked
                ItemComponentType<Object> type = (ItemComponentType<Object>) ItemComponentType.fromId(id);
                Check.notNull(type, "Unknown item component id: {0}", id);
                patch.put(id, type.read(buffer));
            }
            for (int i = 0; i < removed; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                patch.put(id, null);
            }
            return new ItemComponentPatch(patch);
        }
    };
    public static final @NotNull BinaryTagSerializer<ItemComponentPatch> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                if (tag.size() == 0) return EMPTY;
                Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(tag.size());
                for (Map.Entry<String, ? extends BinaryTag> entry : tag) {
                    String key = entry.getKey();
                    boolean remove = false;
                    if (!key.isEmpty() && key.charAt(0) == REMOVAL_PREFIX) {
                        key = key.substring(1);
                        remove = true;
                    }
                    ItemComponentType<?> type = ItemComponentType.fromNamespaceId(key);
                    Check.notNull(type, "Unknown item component: {0}", key);
                    if (remove) {
                        patch.put(type.id(), null);
                    } else {
                        Object value = type.read(entry.getValue());
                        patch.put(type.id(), value);
                    }
                }
                return new ItemComponentPatch(patch);
            },
            patch -> {
                if (patch.patch.isEmpty()) return CompoundBinaryTag.empty();
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Int2ObjectMap.Entry<Object> entry : patch.patch.int2ObjectEntrySet()) {
                    //noinspection unchecked
                    ItemComponentType<Object> type = (ItemComponentType<Object>) ItemComponentType.fromId(entry.getIntKey());
                    Check.notNull(type, "Unknown item component id: {0}", entry.getIntKey());
                    if (entry.getValue() == null) {
                        builder.put(REMOVAL_PREFIX + type.name(), CompoundBinaryTag.empty());
                    } else {
                        builder.put(type.name(), type.write(entry.getValue()));
                    }
                }
                return builder.build();
            }
    );

    public boolean has(@NotNull ItemComponentMap prototype, @NotNull ItemComponentType<?> component) {
        if (patch.containsKey(component.id())) {
            return patch.get(component.id()) != null;
        } else {
            return prototype.has(component);
        }
    }

    public <T> @Nullable T get(@NotNull ItemComponentMap prototype, @NotNull ItemComponentType<T> component) {
        if (patch.containsKey(component.id())) {
            return (T) patch.get(component.id());
        } else {
            return prototype.get(component);
        }
    }

    public <T> @NotNull ItemComponentPatch with(@NotNull ItemComponentType<T> component, @NotNull T value) {
        Int2ObjectMap<Object> newPatch = new Int2ObjectArrayMap<>(patch);
        newPatch.put(component.id(), value);
        return new ItemComponentPatch(newPatch);
    }

    public <T> @NotNull ItemComponentPatch without(@NotNull ItemComponentType<T> component) {
        Int2ObjectMap<Object> newPatch = new Int2ObjectArrayMap<>(patch);
        newPatch.put(component.id(), null);
        return new ItemComponentPatch(newPatch);
    }

    public @NotNull Builder builder() {
        return new Builder(new Int2ObjectArrayMap<>(patch));
    }

    record Builder(@NotNull Int2ObjectMap<Object> patch) implements ItemComponentMap {

        @Override
        public boolean has(@NotNull ItemComponentType<?> component) {
            return patch.get(component.id()) != null;
        }

        @Override
        public <T> @Nullable T get(@NotNull ItemComponentType<T> component) {
            return (T) patch.get(component.id());
        }

        public <T> ItemComponentPatch.@NotNull Builder set(@NotNull ItemComponentType<T> component, @NotNull T value) {
            patch.put(component.id(), value);
            return this;
        }

        public ItemComponentPatch.@NotNull Builder remove(@NotNull ItemComponentType<?> component) {
            patch.put(component.id(), null);
            return this;
        }

        public @NotNull ItemComponentPatch build() {
            return new ItemComponentPatch(patch);
        }
    }
}
