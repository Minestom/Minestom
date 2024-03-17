package net.minestom.server.world;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Allows servers to register custom dimensions. Also used during player login to send the list of all existing dimensions.
 * <p>
 * Contains {@link DimensionType#OVERWORLD} by default but can be removed.
 */
public final class DimensionTypeManager {

    private final List<DimensionType> dimensionTypes = new CopyOnWriteArrayList<>();

    public DimensionTypeManager() {
        addDimension(DimensionType.OVERWORLD);
    }

    /**
     * Adds a new dimension type. This does NOT send the new list to players.
     *
     * @param dimensionType the dimension to add
     */
    public void addDimension(@NotNull DimensionType dimensionType) {
        dimensionType.registered = true;
        this.dimensionTypes.add(dimensionType);
    }

    /**
     * Removes a dimension type. This does NOT send the new list to players.
     *
     * @param dimensionType the dimension to remove
     * @return if the dimension type was removed, false if it was not present before
     */
    public boolean removeDimension(@NotNull DimensionType dimensionType) {
        dimensionType.registered = false;
        return dimensionTypes.remove(dimensionType);
    }

    /**
     * @param namespaceID The dimension name
     * @return true if the dimension is registered
     */
    public boolean isRegistered(@NotNull NamespaceID namespaceID) {
        return isRegistered(getDimension(namespaceID));
    }

    /**
     * @param dimensionType dimension to check if is registered
     * @return true if the dimension is registered
     */
    public boolean isRegistered(@Nullable DimensionType dimensionType) {
        return dimensionType != null && dimensionTypes.contains(dimensionType) && dimensionType.isRegistered();
    }

    /**
     * Return to a @{@link DimensionType} only if present and registered
     *
     * @param namespaceID The Dimension Name
     * @return a DimensionType if it is present and registered
     */
    public @Nullable DimensionType getDimension(@NotNull NamespaceID namespaceID) {
        return unmodifiableList().stream().filter(dimensionType -> dimensionType.getName().equals(namespaceID)).filter(DimensionType::isRegistered).findFirst().orElse(null);
    }

    /**
     * Returns an immutable copy of the dimension types already registered.
     *
     * @return an unmodifiable {@link List} containing all the added dimensions
     */
    public @NotNull List<DimensionType> unmodifiableList() {
        return Collections.unmodifiableList(dimensionTypes);
    }

    /**
     * Creates the {@link CompoundBinaryTag} containing all the registered dimensions.
     * <p>
     * Used when a player connects.
     *
     * @return an nbt compound containing the registered dimensions
     */
    public @NotNull CompoundBinaryTag toNBT() {
        ListBinaryTag.Builder<CompoundBinaryTag> entries = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (DimensionType dimensionType : dimensionTypes)
            entries.add(dimensionType.toIndexedNBT());
        return CompoundBinaryTag.builder()
                .putString("type", "minecraft:dimension_type")
                .put("value", entries.build())
                .build();
    }
}
