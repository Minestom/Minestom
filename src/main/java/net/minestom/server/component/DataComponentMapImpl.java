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
 * <p>A component list, always stored as a patch of added and removed components (even if none are removed).</p>
 *
 * <p>The inner map contains the value for added components, null for removed components, and no entry for unmodified components.</p>
 *
 * @param components The component patch.
 */
record DataComponentMapImpl(@NotNull Int2ObjectMap<Object> components) implements DataComponentMap {
    private static final char REMOVAL_PREFIX = '!';

    public static final @NotNull NetworkBuffer.Type<DataComponentMap> PATCH_NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponentMap value) {
            final DataComponentMapImpl patch = (DataComponentMapImpl) value;
            int added = 0;
            for (Object o : patch.components.values()) {
                if (o != null) added++;
            }

            buffer.write(NetworkBuffer.VAR_INT, added);
            buffer.write(NetworkBuffer.VAR_INT, patch.components.size() - added);
            for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
                if (entry.getValue() != null) {
                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                    //noinspection unchecked
                    DataComponent<Object> type = (DataComponent<Object>) DataComponent.fromId(entry.getIntKey());
                    assert type != null;
                    type.write(buffer, entry.getValue());
                }
            }
            for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
                if (entry.getValue() == null) {
                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                }
            }
        }

        @Override
        public DataComponentMap read(@NotNull NetworkBuffer buffer) {
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
            return new DataComponentMapImpl(patch);
        }
    };
    public static final @NotNull BinaryTagSerializer<DataComponentMap> PATCH_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
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
                return new DataComponentMapImpl(patch);
            },
            value -> {
                final DataComponentMapImpl patch = (DataComponentMapImpl) value;
                if (patch.components.isEmpty()) return CompoundBinaryTag.empty();
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
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

    @Override
    public boolean has(@NotNull DataComponent<?> component) {
        return components.containsKey(component.id()) && components.get(component.id()) != null;
    }

    @Override
    public <T> @Nullable T get(@NotNull DataComponent<T> component) {
        //noinspection unchecked
        return (T) components.get(component.id());
    }

    @Override
    public boolean has(@NotNull DataComponentMap prototype, @NotNull DataComponent<?> component) {
        if (components.containsKey(component.id())) {
            return components.get(component.id()) != null;
        } else {
            return prototype.has(component);
        }
    }

    @Override
    public <T> @Nullable T get(@NotNull DataComponentMap prototype, @NotNull DataComponent<T> component) {
        if (components.containsKey(component.id())) {
            //noinspection unchecked
            return (T) components.get(component.id());
        } else {
            return prototype.get(component);
        }
    }

    @Override
    public @NotNull <T> DataComponentMap set(@NotNull DataComponent<T> component, @NotNull T value) {
        Int2ObjectMap<Object> newComponents = new Int2ObjectArrayMap<>(components);
        newComponents.put(component.id(), value);
        return new DataComponentMapImpl(newComponents);
    }

    @Override
    public @NotNull DataComponentMap remove(@NotNull DataComponent<?> component) {
        Int2ObjectMap<Object> newComponents = new Int2ObjectArrayMap<>(components);
        newComponents.put(component.id(), null);
        return new DataComponentMapImpl(newComponents);
    }

    @Override
    public @NotNull Builder toBuilder() {
        return new BuilderImpl(new Int2ObjectArrayMap<>(components));
    }

    record BuilderImpl(@NotNull Int2ObjectMap<Object> components) implements DataComponentMap.Builder {

        @Override
        public boolean has(@NotNull DataComponent<?> component) {
            return components.get(component.id()) != null;
        }

        @Override
        public <T> @Nullable T get(@NotNull DataComponent<T> component) {
            //noinspection unchecked
            return (T) components.get(component.id());
        }

        @Override
        public <T> @NotNull Builder set(@NotNull DataComponent<T> component, @NotNull T value) {
            components.put(component.id(), value);
            return this;
        }

        @Override
        public @NotNull Builder remove(@NotNull DataComponent<?> component) {
            components.put(component.id(), null);
            return this;
        }

        @Override
        public @NotNull DataComponentMap build() {
            return new DataComponentMapImpl(new Int2ObjectArrayMap<>(components));
        }
    }
}
