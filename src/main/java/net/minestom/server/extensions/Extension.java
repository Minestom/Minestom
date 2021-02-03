package net.minestom.server.extensions;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class Extension {
    // Set by reflection
    @SuppressWarnings("unused")
    private ExtensionDescription description;
    // Set by reflection
    @SuppressWarnings("unused")
    private Logger logger;

    /**
     * Observers that will be notified of events related to this extension.
     * Kept as WeakReference because entities can be observers, but could become candidate to be garbage-collected while
     * this extension holds a reference to it. A WeakReference makes sure this extension does not prevent the memory
     * from being cleaned up.
     */
    private Set<WeakReference<IExtensionObserver>> observers = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
    public ExtensionDescription getDescription() {
        return description;
    }

    @NotNull
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Adds a new observer to this extension.
     * Will be kept as a WeakReference.
     * @param observer
     */
    public void observe(IExtensionObserver observer) {
        observers.add(new WeakReference<>(observer));
    }

    /**
     * Calls some action on all valid observers of this extension
     * @param action code to execute on each observer
     */
    public void triggerChange(Consumer<IExtensionObserver> action) {
        for(WeakReference<IExtensionObserver> weakObserver : observers) {
            IExtensionObserver observer = weakObserver.get();
            if(observer != null) {
                action.accept(observer);
            }
        }
    }

    public static class ExtensionDescription {
        private final String name;
        private final String version;
        private final List<String> authors;
        private final List<String> dependents = new ArrayList<>();
        private final DiscoveredExtension origin;

        ExtensionDescription(@NotNull String name, @NotNull String version, @NotNull List<String> authors, @NotNull DiscoveredExtension origin) {
            this.name = name;
            this.version = version;
            this.authors = authors;
            this.origin = origin;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getVersion() {
            return version;
        }

        @NotNull
        public List<String> getAuthors() {
            return authors;
        }

        @NotNull
        public List<String> getDependents() {
            return dependents;
        }

        @NotNull
        DiscoveredExtension getOrigin() {
            return origin;
        }
    }
}
