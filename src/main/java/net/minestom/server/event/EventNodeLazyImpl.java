package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
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

    private final Object owner;
    @SuppressWarnings("unused")
    private boolean mapped;

    EventNodeLazyImpl(@NotNull Object owner, @NotNull EventFilter<E, ?> filter) {
        super(owner.toString(), filter, null);
        this.owner = owner;
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
    public void map(@NotNull EventNode<? extends E> node, @NotNull Object value) {
        ensureMap();
        super.map(node, value);
    }

    @Override
    public void register(@NotNull EventBinding<? extends E> binding) {
        ensureMap();
        super.register(binding);
    }

    private void ensureMap() {
        if (MAPPED.compareAndSet(this, false, true)) {
            MinecraftServer.getGlobalEventHandler().map(this, owner);
        }
    }
}
