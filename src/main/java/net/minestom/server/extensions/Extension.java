package net.minestom.server.extensions;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class Extension {
    // Set by reflection
    @SuppressWarnings("unused")
    private DiscoveredExtension origin;
    // Set by reflection
    @SuppressWarnings("unused")
    private Logger logger;

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
     * @return The logger for the extension
     */
    @NotNull
    public Logger getLogger() {
        return logger;
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
        while((ref = observerReferenceQueue.poll()) != null) {
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
