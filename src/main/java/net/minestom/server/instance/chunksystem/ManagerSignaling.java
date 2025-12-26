package net.minestom.server.instance.chunksystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class ManagerSignaling {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerSignaling.class);
    private final ReentrantLock signalLock = new ReentrantLock();
    private final Condition hasSignalled = this.signalLock.newCondition();
    /**
     * May only ever be written to from inside the lock, may be read from anywhere.
     * This is volatile, because we expect a high amount of reading with very little writing.
     * This allows us to optimize signalling to not need to lock in some situations.
     */
    private volatile boolean signalled = false;

    public void startIteration() {
        this.signalLock.lock();
        try {
            // set signalled to false, all updates up to here will have been picked up
            this.signalled = false;
        } finally {
            this.signalLock.unlock();
        }
    }

    /**
     * @return true if a problem occurred
     */
    public boolean waitForSignal() {
        // check signaled, if we have already been signaled we can avoid having to lock
        if (this.signalled) return false;
        this.signalLock.lock();
        try {
            // Re-check condition. May have changed since locking, and we don't want to deadlock
            if (this.signalled) return false;
            try {
                // TODO we could also use awaitUninterruptibly, but should we?
                this.hasSignalled.await();
            } catch (InterruptedException e) {
                LOGGER.error("Unexpected interrupt. Someone is meddling with the ChunkClaimManager, this is not allowed! Shutting ChunkClaimManager down!", e);
                return true;
            }
        } finally {
            this.signalLock.unlock();
        }
        return false;
    }

    public void signal() {
        // We can check if the signaled flag is already set.
        // If that is true, then the one full iteration will start, but has not started yet, so we don't have to lock
        // All submitted data will be handled in the next iteration at the latest.
        // Avoiding locking when signaling (when possible) has benefits, because the worker will be able to read with 
        // less contention, and all operations should just happen faster.
        if (this.signalled) return;
        this.signalLock.lock();
        try {
            // re-check signaled, maybe someone else already signaled?
            if (this.signalled) return;
            this.signalled = true;
            // Signal. We have only 1 reader (manager thread), so we don't need signalAll()
            this.hasSignalled.signal();
        } finally {
            this.signalLock.unlock();
        }
    }
}
