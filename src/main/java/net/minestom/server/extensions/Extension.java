package net.minestom.server.extensions;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class Extension {
    // Set by reflection
    @SuppressWarnings("unused")
    private DiscoveredExtension origin;
    // Set by reflection
    @SuppressWarnings("unused")
    private Logger logger;
    // Set by reflection
    @SuppressWarnings("unused")
    private EventNode<Event> eventNode;

    /**
     * Observers that will be notified of events related to this extension.
     * Kept as WeakReference because entities can be observers, but could become candidate to be garbage-collected while
     * this extension holds a reference to it. A WeakReference makes sure this extension does not prevent the memory
     * from being cleaned up.
     */
    protected final Set<WeakReference<IExtensionObserver>> observers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final ReferenceQueue<IExtensionObserver> observerReferenceQueue = new ReferenceQueue<>();

    /**
     * List of extensions that depend on this extension.
     */
    protected final Set<String> dependents = new HashSet<>();

    protected Extension() {

    }

    public void preInitialize() {

    }

    public abstract void initialize();

    public void postInitialize() {

    }

    public void preTerminate() {

    }

    public abstract void terminate();

    public void postTerminate() {

    }

    /**
     * Called after postTerminate when reloading an extension
     */
    public void unload() {

    }

    @NotNull
    public DiscoveredExtension getOrigin() {
        return origin;
    }

    /**
     * Gets the logger for the extension
     *
     * @return The logger for the extension
     */
    @NotNull
    public Logger getLogger() {
        return logger;
    }

    public @NotNull EventNode<Event> getEventNode() {
        return eventNode;
    }

    public @NotNull Path getDataDirectory() {
        return getOrigin().getDataDirectory();
    }

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     * <p>
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getResource(@NotNull String fileName) {
        return getResource(Paths.get(fileName));
    }

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     * <p>
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getResource(@NotNull Path target) {
        final Path targetFile = getDataDirectory().resolve(target);
        try {
            // Copy from jar if the file does not exist in the extension data directory.
            if (!Files.exists(targetFile)) {
                savePackagedResource(target);
            }

            return Files.newInputStream(targetFile);
        } catch (IOException ex) {
            getLogger().info("Failed to read resource {}.", target, ex);
            return null;
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getPackagedResource(@NotNull String fileName) {
        try {
            final URL url = getOrigin().getMinestomExtensionClassLoader().getResource(fileName);
            if (url == null) {
                getLogger().debug("Resource not found: {}", fileName);
                return null;
            }

            return url.openConnection().getInputStream();
        } catch (IOException ex) {
            getLogger().debug("Failed to load resource {}.", fileName, ex);
            return null;
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getPackagedResource(@NotNull Path target) {
        return getPackagedResource(target.toString().replace('\\', '/'));
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param fileName The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    public boolean savePackagedResource(@NotNull String fileName) {
        return savePackagedResource(Paths.get(fileName));
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param target The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    public boolean savePackagedResource(@NotNull Path target) {
        final Path targetFile = getDataDirectory().resolve(target);
        try (InputStream is = getPackagedResource(target)) {
            if (is == null) {
                return false;
            }

            Files.createDirectories(targetFile.getParent());
            Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            getLogger().debug("Failed to save resource {}.", target, ex);
            return false;
        }
    }

    /**
     * Adds a new observer to this extension.
     * Will be kept as a WeakReference.
     *
     * @param observer The observer to add
     */
    public void observe(IExtensionObserver observer) {
        observers.add(new WeakReference<>(observer, observerReferenceQueue));
    }

    /**
     * Calls some action on all valid observers of this extension
     *
     * @param action code to execute on each observer
     */
    public void triggerChange(Consumer<IExtensionObserver> action) {
        for (WeakReference<IExtensionObserver> weakObserver : observers) {
            IExtensionObserver observer = weakObserver.get();
            if (observer != null) {
                action.accept(observer);
            }
        }
    }

    /**
     * If this extension registers code modifiers and/or mixins, are they loaded correctly?
     */
    public boolean areCodeModifiersAllLoadedCorrectly() {
        return !getOrigin().hasFailedToLoadMixin() && getOrigin().getMissingCodeModifiers().isEmpty();
    }

    /**
     * Removes all expired reference to observers
     *
     * @see #observers
     */
    public void cleanupObservers() {
        Reference<? extends IExtensionObserver> ref;
        while ((ref = observerReferenceQueue.poll()) != null) {
            observers.remove(ref);
        }
    }

    /**
     * @return A modifiable list of dependents.
     */
    public Set<String> getDependents() {
        return dependents;
    }
}
