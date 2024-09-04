package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

final class EventNodeLazyImpl<E extends Event> extends EventNodeImpl<E> {
    private static final VarHandle MAPPED;

    static {
        try {
            MAPPED = MethodHandles.lookup().findVarHandle(EventNodeLazyImpl.class, "mapped", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final EventNodeImpl<? super E> holder;
    private final WeakReference<Object> owner;
    @SuppressWarnings("unused")
    private boolean mapped;

    EventNodeLazyImpl(@NotNull EventNodeImpl<? super E> holder,
                      @NotNull Object owner, @NotNull EventFilter<E, ?> filter) {
        super(owner.toString(), filter, null);
        this.holder = holder;
        this.owner = new WeakReference<>(owner);
    }

    @Override
    public @NotNull EventNode<E> addChild(@NotNull EventNode<? extends E> child) {
        ensureMap();
        return super.addChild(child);
    }

    @Override
    public @NotNull EventNode<E> addListener(@NotNull EventListener<? extends E> listener) {
        ensureMap();
        return super.addListener(listener);
    }

    @Override
    public @NotNull <E1 extends E> EventNode<E> addListener(@NotNull Class<E1> eventType, @NotNull Consumer<@NotNull E1> listener) {
        ensureMap();
        return super.addListener(eventType, listener);
    }

    @Override
    public @NotNull <E1 extends E, H> EventNode<E1> map(@NotNull H value, @NotNull EventFilter<E1, H> filter) {
        final Object owner = retrieveOwner();
        if (owner != value) {
            throw new IllegalArgumentException("Cannot map an object to an already mapped node.");
        }
        return (EventNode<E1>) this;
    }

    @Override
    public void register(@NotNull EventBinding<? extends E> binding) {
        ensureMap();
        super.register(binding);
    }

    private void ensureMap() {
        if (MAPPED.compareAndSet(this, false, true)) {
            synchronized (GLOBAL_CHILD_LOCK) {
                Map registered = new WeakHashMap<>(this.holder.registeredMappedNode);
                var previous = registered.putIfAbsent(retrieveOwner(),
                        new WeakReference<>(EventNodeLazyImpl.class.cast(this)));
                this.holder.registeredMappedNode = registered;
                if (previous == null) invalidateEventsFor(holder);
            }
        }
    }

    private Object retrieveOwner() {
        final Object owner = this.owner.get();
        if (owner == null) {
            throw new IllegalStateException("Node handle is null. Be sure to never cache a local node.");
        }
        return owner;
    }
}
