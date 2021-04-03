package net.minestom.testing.framework;

import net.minestom.server.MinecraftServer;
import net.minestom.testing.miniclient.MiniClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class TestEnvironment implements Closeable, AutoCloseable {
    private final Semaphore semaphore;
    private final int maxTestCount;
    // TODO: server, objects to create clients, etc.
    // TODO: find better name

    TestEnvironment(int testCount) {
        this.maxTestCount = testCount;
        semaphore = new Semaphore(testCount);
        semaphore.drainPermits();
        MinecraftServer server = MinecraftServer.init/*Isolated*/();

        server.start("localhost", 25565); // TODO: random port?
    }

    public MiniClient newClient() {
        // TODO
        return null;
    }

    public void waitNetworkIdle() {

    }

    @Override
    public void close() throws IOException {
        semaphore.release();

        // stop server when all tests using this environment are done
        if(semaphore.availablePermits() == maxTestCount) {
            MinecraftServer.stopCleanly();
        }
    }

    /**
     * Thread-safe lazy supplier of TestEnvironment
     */
    public static class Lazy implements Supplier<TestEnvironment> {

        private final int count;
        private TestEnvironment value = null;
        private final Object lock = new Object();

        public Lazy(int count) {
            this.count = count;
        }

        @Override
        public TestEnvironment get() {
            if(value == null) {
                synchronized (lock) {
                    if(value == null) {
                        value = new TestEnvironment(count);
                    }
                }
            }
            return value;
        }
    }
}
