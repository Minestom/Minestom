package net.minestom.server.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * <p>Represents any container of {@link DataComponent}s.</p>
 *
 * <p>This type is capable of storing a patch of added and removed components on top of a 'prototype', or existing
 * set of components. See {@link #diff(DataComponentMap, DataComponentMap)}.</p>
 */
@ApiStatus.Experimental
public sealed interface DataComponentMap extends DataComponent.Holder permits DataComponentMapImpl {
    @NotNull DataComponentMap EMPTY = new DataComponentMapImpl(new Int2ObjectArrayMap<>(0));

    static @NotNull DataComponentMap.Builder builder() {
        return new DataComponentMapImpl.BuilderImpl(new Int2ObjectArrayMap<>());
    }

    static @NotNull DataComponentMap.PatchBuilder patchBuilder() {
        return new DataComponentMapImpl.PatchBuilderImpl(new Int2ObjectArrayMap<>());
    }

    /**
     * Creates a network type for the given component type. For internal use only, get the value from the target component class.
     */
    @ApiStatus.Internal
    static @NotNull BinaryTagSerializer<DataComponentMap> nbtType(
            @NotNull IntFunction<DataComponent<?>> idToType,
            @NotNull Function<String, DataComponent<?>> nameToType
    ) {
        return new DataComponentMapImpl.NbtType(idToType, nameToType, false);
    }

    /**
     * Creates a network type for the given component type. For internal use only, get the value from the target component class.
     */
    @ApiStatus.Internal
    static @NotNull NetworkBuffer.Type<DataComponentMap> patchNetworkType(@NotNull IntFunction<DataComponent<?>> idToType) {
        return new DataComponentMapImpl.PatchNetworkType(idToType);
    }

    /**
     * Creates a network type for the given component type. For internal use only, get the value from the target component class.
     */
    @ApiStatus.Internal
    static @NotNull BinaryTagSerializer<DataComponentMap> patchNbtType(
            @NotNull IntFunction<DataComponent<?>> idToType,
            @NotNull Function<String, DataComponent<?>> nameToType
    ) {
        return new DataComponentMapImpl.NbtType(idToType, nameToType, true);
    }

    static @NotNull DataComponentMap diff(@NotNull DataComponentMap prototype, @NotNull DataComponentMap patch) {
        final DataComponentMapImpl patchImpl = (DataComponentMapImpl) patch;
        if (patchImpl.components().isEmpty()) return EMPTY;

        final DataComponentMapImpl protoImpl = (DataComponentMapImpl) prototype;

        final Int2ObjectArrayMap<Object> diff = new Int2ObjectArrayMap<>(patchImpl.components());
        var iter = diff.int2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            final var entry = iter.next(); // Entry in patch
            final var protoComp = protoImpl.components().get(entry.getIntKey()); // Entry in prototype
            if (entry.getValue() == null) {
                // If the component is removed, remove it from the diff if it is not in the prototype
                if (!protoImpl.components().containsKey(entry.getIntKey())) {
                    iter.remove();
                }
            } else if (protoComp != null && protoComp.equals(entry.getValue())) {
                // If the component is the same as in the prototype, remove it from the diff
                iter.remove();
            }
        }

        return new DataComponentMapImpl(diff);
    }

    /**
     * Does a 'patch'ed has against the given prototype. That is, this map is treated as the primary source, but if
     * unspecified, the given prototype is used as a fallback.
     *
     * @param prototype The prototype to fall back to
     * @param component The component to check
     * @return True if the component is present (taking into account the prototype).
     */
    boolean has(@NotNull DataComponentMap prototype, @NotNull DataComponent<?> component);

    /**
     * Does a 'patch'ed get against the given prototype. That is, this map is treated as the primary source, but if
     * unspecified, the given prototype is used as a fallback.
     *
     * @param prototype The prototype to fall back to
     * @param component The component to get
     * @return The value of the component, or null if not present (taking into account the prototype).
     * @param <T> The type of the component
     */
    <T> @Nullable T get(@NotNull DataComponentMap prototype, @NotNull DataComponent<T> component);

    /**
     * Adds the component, overwriting any prior value if present.
     *
     * @return A new map with the component set to the value
     */
    <T> @NotNull DataComponentMap set(@NotNull DataComponent<T> component, @NotNull T value);

    default @NotNull DataComponentMap set(@NotNull DataComponent<Unit> component) {
        return set(component, Unit.INSTANCE);
    }

    /**
     * Removes the component from the map (or patch).
     *
     * @param component The component to remove
     * @return A new map with the component removed
     */
    @NotNull DataComponentMap remove(@NotNull DataComponent<?> component);

    @NotNull Builder toBuilder();

    @NotNull PatchBuilder toPatchBuilder();

    sealed interface Builder extends DataComponent.Holder permits DataComponentMapImpl.BuilderImpl {

        <T> @NotNull Builder set(@NotNull DataComponent<T> component, @NotNull T value);

        default @NotNull Builder set(@NotNull DataComponent<Unit> component) {
            return set(component, Unit.INSTANCE);
        }

        @NotNull DataComponentMap build();

    }

    sealed interface PatchBuilder extends DataComponent.Holder permits DataComponentMapImpl.PatchBuilderImpl {

        <T> @NotNull PatchBuilder set(@NotNull DataComponent<T> component, @NotNull T value);

        default @NotNull PatchBuilder set(@NotNull DataComponent<Unit> component) {
            return set(component, Unit.INSTANCE);
        }

        @NotNull PatchBuilder remove(@NotNull DataComponent<?> component);

        @NotNull DataComponentMap build();

    }

}
