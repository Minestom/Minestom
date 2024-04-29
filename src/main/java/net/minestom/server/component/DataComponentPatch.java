package net.minestom.server.component;

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
public record DataComponentPatch(@NotNull Int2ObjectMap<Object> patch) {
    private static final char REMOVAL_PREFIX = '!';

    public static final DataComponentPatch EMPTY = new DataComponentPatch(new Int2ObjectArrayMap<>(0));

    public static final @NotNull NetworkBuffer.Type<DataComponentPatch> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponentPatch value) {
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
                    DataComponent<Object> type = (DataComponent<Object>) DataComponent.fromId(entry.getIntKey());
                    assert type != null;
                    type.write(buffer, entry.getValue());
                }
            }
            for (Int2ObjectMap.Entry<Object> entry : value.patch.int2ObjectEntrySet()) {
                if (entry.getValue() == null) {
                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                }
            }
        }

        @Override
        public DataComponentPatch read(@NotNull NetworkBuffer buffer) {
            int added = buffer.read(NetworkBuffer.VAR_INT);
            int removed = buffer.read(NetworkBuffer.VAR_INT);
            Check.stateCondition(added + removed > DataComponent.values().size() * 2, "Item component patch too large: {0}", added + removed);
            Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(added + removed);
            for (int i = 0; i < added; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                //noinspection unchecked
                DataComponent<Object> type = (DataComponent<Object>) DataComponent.fromId(id);
                Check.notNull(type, "Unknown item component id: {0}", id);
                patch.put(id, type.read(buffer));
            }
            for (int i = 0; i < removed; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                patch.put(id, null);
            }
            return new DataComponentPatch(patch);
        }
    };
    public static final @NotNull BinaryTagSerializer<DataComponentPatch> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
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
                    DataComponent<?> type = DataComponent.fromNamespaceId(key);
                    Check.notNull(type, "Unknown item component: {0}", key);
                    if (remove) {
                        patch.put(type.id(), null);
                    } else {
                        Object value = type.read(entry.getValue());
                        patch.put(type.id(), value);
                    }
                }
                return new DataComponentPatch(patch);
            },
            patch -> {
                if (patch.patch.isEmpty()) return CompoundBinaryTag.empty();
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Int2ObjectMap.Entry<Object> entry : patch.patch.int2ObjectEntrySet()) {
                    //noinspection unchecked
                    DataComponent<Object> type = (DataComponent<Object>) DataComponent.fromId(entry.getIntKey());
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

    public static @NotNull DataComponentPatch from(@NotNull DataComponentMap prototype, @NotNull DataComponentMap components) {
        return new DataComponentPatch(((DataComponentMapImpl) components).components());
    }

    public boolean has(@NotNull DataComponentMap prototype, @NotNull DataComponent<?> component) {
        if (patch.containsKey(component.id())) {
            return patch.get(component.id()) != null;
        } else {
            return prototype.has(component);
        }
    }

    public <T> @Nullable T get(@NotNull DataComponentMap prototype, @NotNull DataComponent<T> component) {
        if (patch.containsKey(component.id())) {
            return (T) patch.get(component.id());
        } else {
            return prototype.get(component);
        }
    }

    public <T> @NotNull DataComponentPatch with(@NotNull DataComponent<T> component, @NotNull T value) {
        Int2ObjectMap<Object> newPatch = new Int2ObjectArrayMap<>(patch);
        newPatch.put(component.id(), value);
        return new DataComponentPatch(newPatch);
    }

    public <T> @NotNull DataComponentPatch without(@NotNull DataComponent<T> component) {
        Int2ObjectMap<Object> newPatch = new Int2ObjectArrayMap<>(patch);
        newPatch.put(component.id(), null);
        return new DataComponentPatch(newPatch);
    }

    public @NotNull Builder builder() {
        return new Builder(new Int2ObjectArrayMap<>(patch));
    }

    public record Builder(@NotNull Int2ObjectMap<Object> patch) implements DataComponentMap {

        @Override
        public boolean has(@NotNull DataComponent<?> component) {
            return patch.get(component.id()) != null;
        }

        @Override
        public <T> @Nullable T get(@NotNull DataComponent<T> component) {
            return (T) patch.get(component.id());
        }

        public <T> DataComponentPatch.@NotNull Builder set(@NotNull DataComponent<T> component, @NotNull T value) {
            patch.put(component.id(), value);
            return this;
        }

        public DataComponentPatch.@NotNull Builder remove(@NotNull DataComponent<?> component) {
            patch.put(component.id(), null);
            return this;
        }

        public @NotNull DataComponentPatch build() {
            return new DataComponentPatch(new Int2ObjectArrayMap<>(this.patch));
        }
    }
}
