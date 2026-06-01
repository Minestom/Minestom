package net.minestom.server.instance.chunksystem;

import net.minestom.server.MinecraftServer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class ManagerSignaling {
    private final ReentrantLock signalLock = new ReentrantLock();
    private final Condition hasSignalled = this.signalLock.newCondition();
    /**
     * May only ever be written to from inside the lock, may be read from anywhere.
     * This is volatile because we expect a high amount of reading with very little writing.
     * This allows us to optimize signaling to not need to lock in some situations.
     */
    private volatile boolean signaled = false;

    public void startIteration() {
        this.signalLock.lock();
        try {
            // set signaled to false. All updates up to here will have been picked up
            this.signaled = false;
        } finally {
            this.signalLock.unlock();
        }
    }

    /**
     * @return true if a problem occurred
     */
    public boolean waitForSignal() {
        // check signaled, if we have already been signaled, we can avoid having to lock
        if (this.signaled) return false;
        this.signalLock.lock();
        try {
            // Re-check condition. May have changed since locking, and we don't want to deadlock
            if (this.signaled) return false;
            try {
                // TODO we could also use awaitUninterruptibly, but should we?
                this.hasSignalled.await();
            } catch (InterruptedException e) {
                MinecraftServer.getExceptionManager().handleException(new ChunkSystemException("Unexpected interrupt. Someone is meddling with the ChunkClaimManager, this is not allowed!", e));
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
        // Avoiding locking when signaling (when possible) has benefits because the worker will be able to read with
        // less contention, and all operations should just happen faster.
        if (this.signaled) return;
        this.signalLock.lock();
        try {
            // re-check signaled. Maybe someone else already signaled?
            if (this.signaled) return;
            this.signaled = true;
            // Signal. We have only 1 reader (manager thread), so we don't need signalAll()
            this.hasSignalled.signal();
        } finally {
            this.signalLock.unlock();
        }
    }
}
