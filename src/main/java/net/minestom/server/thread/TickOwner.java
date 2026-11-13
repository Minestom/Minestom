package net.minestom.server.thread;

import net.minestom.server.entity.Entity;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Tick loop of an external system owning entities, registered through
 * {@link Entity#setExternallyTicked(TickOwner)} so that {@link Acquirable} stays valid on them.
 * <p>
 * Acquisition locks {@link #lock()} the same way it locks a {@link TickThread}'s: the owner's loop
 * must hold it while ticking its elements, and must not hold it while acquiring foreign elements.
 */
public interface TickOwner {

    /**
     * Gets the lock held by the owner's loop while it ticks its elements.
     */
    ReentrantLock lock();

    /**
     * Gets whether the calling thread is the owner's tick thread.
     */
    boolean isCurrentThread();
}
