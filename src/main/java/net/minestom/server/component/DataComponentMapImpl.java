package net.minestom.server.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.codec.Transcoder.MapLike;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.validate.Check;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * <p>A component list, always stored as a patch of added and removed components (even if none are removed).</p>
 *
 * <p>The inner map contains the value for added components, null for removed components, and no entry for unmodified components.</p>
 *
 * @param components The component patch.
 */
record DataComponentMapImpl(Int2ObjectMap<Object> components) implements DataComponentMap {
    private static final char REMOVAL_PREFIX = '!';

    @Override
    public boolean isEmpty() {
        return components.isEmpty();
    }

    @Override
    public boolean has(DataComponent<?> component) {
        return components.containsKey(component.id()) && components.get(component.id()) != null;
    }

    @Override
    public <T> @Nullable T get(DataComponent<T> component) {
        //noinspection unchecked
        return (T) components.get(component.id());
    }

    @Override
    public boolean has(DataComponentMap prototype, DataComponent<?> component) {
        if (components.containsKey(component.id())) {
            return components.get(component.id()) != null;
        } else {
            return prototype.has(component);
        }
    }

    @Override
    public <T> @Nullable T get(DataComponentMap prototype, DataComponent<T> component) {
        if (components.containsKey(component.id())) {
            //noinspection unchecked
            return (T) components.get(component.id());
        } else {
            return prototype.get(component);
        }
    }

    @Override
    public <T> DataComponentMap set(DataComponent<T> component, T value) {
        Int2ObjectMap<Object> newComponents = new Int2ObjectArrayMap<>(components);
        newComponents.put(component.id(), value);
        return new DataComponentMapImpl(newComponents);
    }

    @Override
    public DataComponentMap remove(DataComponent<?> component) {
        Int2ObjectMap<Object> newComponents = new Int2ObjectArrayMap<>(components);
        newComponents.put(component.id(), null);
        return new DataComponentMapImpl(newComponents);
    }

    @Override
    public Collection<DataComponent.Value> entrySet() {
        if (components.isEmpty()) return List.of();
        final List<DataComponent.Value> entries = new ArrayList<>(components.size());
        for (var entry : components.int2ObjectEntrySet())
            entries.add(new DataComponent.Value(DataComponent.fromId(entry.getIntKey()), entry.getValue()));
        return entries;
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(new Int2ObjectArrayMap<>(components));
    }

    @Override
    public PatchBuilder toPatchBuilder() {
        return new PatchBuilderImpl(new Int2ObjectArrayMap<>(components));
    }

    record BuilderImpl(Int2ObjectMap<Object> components) implements DataComponentMap.Builder {

        @Override
        public boolean has(DataComponent<?> component) {
            return components.get(component.id()) != null;
        }

        @Override
        public <T> @Nullable T get(DataComponent<T> component) {
            //noinspection unchecked
            return (T) components.get(component.id());
        }

        @Override
        public <T> Builder set(DataComponent<T> component, T value) {
            components.put(component.id(), value);
            return this;
        }

        @Override
        public DataComponentMap build() {
            return new DataComponentMapImpl(new Int2ObjectArrayMap<>(components));
        }
    }

    record PatchBuilderImpl(Int2ObjectMap<Object> components) implements DataComponentMap.PatchBuilder {

        @Override
        public boolean has(DataComponent<?> component) {
            return components.get(component.id()) != null;
        }

        @Override
        public <T> @Nullable T get(DataComponent<T> component) {
            //noinspection unchecked
            return (T) components.get(component.id());
        }

        @Override
        public <T> PatchBuilder set(DataComponent<T> component, T value) {
            components.put(component.id(), value);
            return this;
        }

        @Override
        public PatchBuilder remove(DataComponent<?> component) {
            components.put(component.id(), null);
            return this;
        }

        @Override
        public DataComponentMap build() {
            return new DataComponentMapImpl(new Int2ObjectArrayMap<>(components));
        }
    }

    record NetworkTypeImpl(
            IntFunction<DataComponent<?>> idToType,
            boolean isPatch, boolean isTrusted
    ) implements NetworkBuffer.Type<DataComponentMap> {
        @Override
        public void write(NetworkBuffer buffer, DataComponentMap value) {
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
                if (isTrusted) {
                    type.write(buffer, entry.getValue());
                } else {
                    // Need to length prefix it, so write to another buffer first then copy.
                    final byte[] componentData = NetworkBuffer.makeArray(b -> type.write(b, entry.getValue()), buffer.registries());
                    buffer.write(NetworkBuffer.BYTE_ARRAY, componentData);
                }
            }
            if (isPatch) {
                for (Int2ObjectMap.Entry<Object> entry : patch.components.int2ObjectEntrySet()) {
                    if (entry.getValue() != null) continue;

                    buffer.write(NetworkBuffer.VAR_INT, entry.getIntKey());
                }
            }
        }

        @Override
        public DataComponentMap read(NetworkBuffer buffer) {
            int added = buffer.read(NetworkBuffer.VAR_INT);
            int removed = isPatch ? buffer.read(NetworkBuffer.VAR_INT) : 0;
            Check.stateCondition(added + removed > 256, "Data component map too large: {0}", added + removed);
            Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(added + removed);
            for (int i = 0; i < added; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                //noinspection unchecked
                DataComponent<Object> type = (DataComponent<Object>) this.idToType.apply(id);
                Check.notNull(type, "Unknown component: {0}", id);
                if (isTrusted) {
                    patch.put(type.id(), type.read(buffer));
                } else {
                    final byte[] array = buffer.read(NetworkBuffer.BYTE_ARRAY);
                    final NetworkBuffer tempBuffer = NetworkBuffer.wrap(array, 0, array.length, buffer.registries());
                    patch.put(type.id(), type.read(tempBuffer));
                }
            }
            for (int i = 0; i < removed; i++) {
                int id = buffer.read(NetworkBuffer.VAR_INT);
                patch.put(id, null);
            }
            return new DataComponentMapImpl(patch);
        }
    }

    record CodecImpl(
            IntFunction<DataComponent<?>> idToType,
            Function<String, DataComponent<?>> nameToType,
            boolean isPatch
    ) implements Codec<DataComponentMap> {
        @Override
        public <D> Result<DataComponentMap> decode(Transcoder<D> coder, D value) {
            final Result<MapLike<D>> mapResult = coder.getMap(value);
            if (!(mapResult instanceof Result.Ok(var map)))
                return mapResult.cast();
            if (map.isEmpty()) return new Result.Ok<>(EMPTY);

            final Int2ObjectMap<Object> patch = new Int2ObjectArrayMap<>(map.size());
            for (String key : map.keys()) {
                boolean remove = false;
                if (!key.isEmpty() && key.charAt(0) == REMOVAL_PREFIX) {
                    key = key.substring(1);
                    remove = true;
                }
                final DataComponent<?> type = this.nameToType.apply(key);
                if (type == null) return new Result.Error<>("unknown data component: " + key);

                if (remove) {
                    if (isPatch) patch.put(type.id(), null);
                    // Removing a component in an absolute (non-patch) builder is a noop because it is not yet present.
                } else {
                    switch (map.getValue(key).map(v -> type.decode(coder, v))) {
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
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable DataComponentMap value) {
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
