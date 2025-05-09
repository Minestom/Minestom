package net.minestom.server.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * <p>A component list, always stored as a patch of added and removed components (even if none are removed).</p>
 *
 * <p>The inner map contains the value for added components, null for removed components, and no entry for unmodified components.</p>
 *
 * @param components The component patch.
 */
record DataComponentMapImpl(@NotNull Int2ObjectMap<Object> components) implements DataComponentMap {
    private static final char REMOVAL_PREFIX = '!';

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

    @Override
    public @NotNull PatchBuilder toPatchBuilder() {
        return new PatchBuilderImpl(new Int2ObjectArrayMap<>(components));
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
        public @NotNull DataComponentMap build() {
            return new DataComponentMapImpl(new Int2ObjectArrayMap<>(components));
        }
    }

    record PatchBuilderImpl(@NotNull Int2ObjectMap<Object> components) implements DataComponentMap.PatchBuilder {

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
        public <T> @NotNull PatchBuilder set(@NotNull DataComponent<T> component, @NotNull T value) {
            components.put(component.id(), value);
            return this;
        }

        @Override
        public @NotNull PatchBuilder remove(@NotNull DataComponent<?> component) {
            components.put(component.id(), null);
            return this;
        }

        @Override
        public @NotNull DataComponentMap build() {
            return new DataComponentMapImpl(new Int2ObjectArrayMap<>(components));
        }
    }

    record NetworkTypeImpl(
            @NotNull IntFunction<DataComponent<?>> idToType,
            boolean isPatch
    ) implements NetworkBuffer.Type<DataComponentMap> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponentMap value) {
            final DataComponentMapImpl patch = (DataComponentMapImpl) value;
            int added = 0;
            for (Object o : patch.components.values()) {
                if (o != null) added++;
            }

            buffer.write(NetworkBuffer.VAR_INT, added);
            if (isPatch) {
                buffer.write(NetworkBuffer.VAR_INT, patch.components.size() - added);
            }
            for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
                if (entry.getValue() == null) continue;

                buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                //noinspection unchecked
                DataComponent<Object> type = (DataComponent<Object>) this.idToType.apply(entry.getIntKey());
                assert type != null;
                type.write(buffer, entry.getValue());
            }
            if (isPatch) {
                for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
                    if (entry.getValue() != null) continue;

                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                }
            }
        }

        @Override
        public DataComponentMap read(@NotNull NetworkBuffer buffer) {
            int added = buffer.read(NetworkBuffer.VAR_INT);
            int removed = isPatch ? buffer.read(NetworkBuffer.VAR_INT) : 0;
            Check.stateCondition(added + removed > 256, "Data component map too large: {0}", added + removed);
            Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(added + removed);
            for (int i = 0; i < added; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                //noinspection unchecked
                DataComponent<Object> type = (DataComponent<Object>) this.idToType.apply(id);
                Check.notNull(type, "Unknown component: {0}", id);
                patch.put(type.id(), type.read(buffer));
            }
            for (int i = 0; i < removed; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                patch.put(id, null);
            }
            return new DataComponentMapImpl(patch);
        }
    }

    record CodecImpl(
            @NotNull IntFunction<DataComponent<?>> idToType,
            @NotNull Function<String, DataComponent<?>> nameToType,
            boolean isPatch
    ) implements Codec<DataComponentMap> {
        @Override
        public @NotNull <D> Result<DataComponentMap> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final var entriesResult = coder.getMapEntries(value);
            if (!(entriesResult instanceof Result.Ok(var entries)))
                return entriesResult.cast();
            if (entries.isEmpty()) return new Result.Ok<>(EMPTY);

            Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(entries.size());
            for (Map.Entry<String, D> entry : entries) {
                String key = entry.getKey();
                boolean remove = false;
                if (!key.isEmpty() && key.charAt(0) == REMOVAL_PREFIX) {
                    key = key.substring(1);
                    remove = true;
                }
                DataComponent<?> type = this.nameToType.apply(key);
                Check.notNull(type, "Unknown item component: {0}", key);
                if (remove) {
                    if (isPatch) patch.put(type.id(), null);
                    // Removing a component in an absolute (non-patch) builder is a noop because it is not yet present.
                } else {
                    switch (type.decode(coder, entry.getValue())) {
                        case Result.Ok(Object componentData) -> patch.put(type.id(), componentData);
                        case Result.Error<?>(String message) -> {
                            return new Result.Error<>(type.name() + ": " + message);
                        }
                    }
                }
            }

            return new Result.Ok<>(new DataComponentMapImpl(patch));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable DataComponentMap value) {
            final DataComponentMapImpl patch = (DataComponentMapImpl) value;

            final Transcoder.MapBuilder<D> map = coder.createMap();
            for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
                //noinspection unchecked
                DataComponent<Object> type = (DataComponent<Object>) this.idToType.apply(entry.getIntKey());
                if (type == null) return new Result.Error<>("unknown data component id: " + entry.getIntKey());
                if (entry.getValue() == null) {
                    if (isPatch) map.put(REMOVAL_PREFIX + type.name(), coder.createMap().build());
                    // Removing a component in an absolute (non-patch) builder is a noop because it is not yet present.
                } else {
                    switch (type.encode(coder, entry.getValue())) {
                        case Result.Ok(D componentValue) -> map.put(type.name(), componentValue);
                        case Result.Error<?>(String message) -> {
                            return new Result.Error<>(type.name() + ": " + message);
                        }
                    }
                }
            }

            return new Result.Ok<>(map.build());
        }
    }

}
