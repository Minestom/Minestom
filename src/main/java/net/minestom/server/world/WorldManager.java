package net.minestom.server.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Used to register {@link World}.
 */
public final class WorldManager {

    private final Set<World> worlds = new CopyOnWriteArraySet<>();

    /**
     * Registers an {@link World} internally.
     * <p>
     * Note: not necessary if you created your World using {@link #createWorldContainer()} or {@link #createSharedWorld(WorldContainer)}
     * but only if you instantiated your World object manually
     *
     * @param world the {@link World} to register
     */
    public void registerWorld(@NotNull World world) {
        Check.stateCondition(world instanceof SharedWorld,
                "Please use WorldManager#registerSharedWorld to register a shared World");
        UNSAFE_registerWorld(world);
    }

    /**
     * Creates and register an {@link WorldContainer}
     * with the specified {@link DimensionType} and {@link StorageLocation}.
     *
     * @param dimensionType   the {@link DimensionType} of the World
     * @param storageLocation the {@link StorageLocation} of the World, can be null
     * @return the created {@link WorldContainer}
     */
    @NotNull
    public WorldContainer createWorldContainer(@NotNull DimensionType dimensionType, @Nullable StorageLocation storageLocation) {
        final WorldContainer worldContainer = new WorldContainer(UUID.randomUUID(), dimensionType, storageLocation);
        registerWorld(worldContainer);
        return worldContainer;
    }

    /**
     * Creates and register an {@link WorldContainer} with the specified {@link StorageLocation}.
     *
     * @param storageLocation the {@link StorageLocation} of the World, can be null
     * @return the created {@link WorldContainer}
     */
    @NotNull
    public WorldContainer createWorldContainer(@Nullable StorageLocation storageLocation) {
        return createWorldContainer(DimensionType.OVERWORLD, storageLocation);
    }

    /**
     * Creates and register an {@link WorldContainer} with the specified {@link DimensionType}.
     *
     * @param dimensionType the {@link DimensionType} of the World
     * @return the created {@link WorldContainer}
     */
    @NotNull
    public WorldContainer createWorldContainer(@NotNull DimensionType dimensionType) {
        return createWorldContainer(dimensionType, null);
    }

    /**
     * Creates and register an {@link WorldContainer}.
     *
     * @return the created {@link WorldContainer}
     */
    @NotNull
    public WorldContainer createWorldContainer() {
        return createWorldContainer(DimensionType.OVERWORLD);
    }

    /**
     * Registers a {@link SharedWorld}.
     * <p>
     * WARNING: the {@link SharedWorld} needs to have an {@link WorldContainer} assigned to it.
     *
     * @param sharedWorld the {@link SharedWorld} to register
     * @return the registered {@link SharedWorld}
     * @throws NullPointerException if {@code sharedWorld} doesn't have an {@link WorldContainer} assigned to it
     */
    @NotNull
    public SharedWorld registerSharedWorled(@NotNull SharedWorld sharedWorld) {
        final WorldContainer worldContainer = sharedWorld.getWorldContainer();
        Check.notNull(worldContainer, "SharedWorld needs to have an WorldContainer to be created!");

        worldContainer.addSharedWorld(sharedWorld);
        UNSAFE_registerWorld(sharedWorld);
        return sharedWorld;
    }

    /**
     * Creates and register a {@link SharedWorld}.
     *
     * @param worldContainer the container assigned to the {@link SharedWorld}
     * @return the created {@link SharedWorld}
     * @throws IllegalStateException if {@code worldContainer} is not registered
     */
    @NotNull
    public SharedWorld createSharedWorld(@NotNull WorldContainer worldContainer) {
        Check.notNull(worldContainer, "World container cannot be null when creating a SharedWorld!");
        Check.stateCondition(!worldContainer.isRegistered(), "The container needs to be register in the WorldManager");

        final SharedWorld sharedWorld = new SharedWorld(UUID.randomUUID(), worldContainer);
        return registerSharedWorled(sharedWorld);
    }

    /**
     * Unregisters the {@link World} internally.
     * <p>
     * If {@code world} is an {@link WorldContainer} all chunks are unloaded.
     *
     * @param world the {@link World} to unregister
     */
    public void unregisterWorld(@NotNull World world) {
        Check.stateCondition(!world.getPlayers().isEmpty(), "You cannot unregister an World with players inside.");

        synchronized (world) {
            // Unload all chunks
            if (world instanceof WorldContainer) {
                WorldContainer worldContainer = (WorldContainer) world;

                Set<Chunk> scheduledChunksToRemove = worldContainer.scheduledChunksToRemove;
                synchronized (scheduledChunksToRemove) {
                    scheduledChunksToRemove.addAll(worldContainer.getChunks());
                    worldContainer.UNSAFE_unloadChunks();
                }
            }

            world.setRegistered(false);
            this.worlds.remove(world);
            MinecraftServer.getUpdateManager().signalWorldDelete(world);
        }
    }

    /**
     * Gets all the registered Worlds.
     *
     * @return an unmodifiable {@link Set} containing all the registered Worlds
     */
    @NotNull
    public Set<World> getWorlds() {
        return Collections.unmodifiableSet(worlds);
    }

    /**
     * Gets a World by the given UUID.
     *
     * @param uuid UUID of the World
     * @return the World with the given UUID, null if not found
     */
    @Nullable
    public World getWorld(@NotNull UUID uuid) {
        Optional<World> world = getWorlds()
                .stream()
                .filter(someWorld -> someWorld.getUniqueId().equals(uuid))
                .findFirst();
        return world.orElse(null);
    }

    /**
     * Registers an {@link World} internally.
     * <p>
     * Unsafe because it does not check if {@code world} is a {@link SharedWorld} to verify its container.
     *
     * @param world the {@link World} to register
     */
    private void UNSAFE_registerWorld(@NotNull World world) {
        world.setRegistered(true);
        this.worlds.add(world);
        MinecraftServer.getUpdateManager().signalWorldCreate(world);
    }

}
