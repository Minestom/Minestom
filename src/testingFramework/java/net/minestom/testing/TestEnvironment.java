package net.minestom.testing;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.testing.miniclient.MiniClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class TestEnvironment implements Closeable, AutoCloseable {
    private final Semaphore semaphore;
    private final int maxTestCount;
    private final Map<String, MiniClient> clients = new ConcurrentHashMap<>();
    // TODO: server, objects to create clients, etc.
    // TODO: find better name

    private final AtomicInteger counter = new AtomicInteger();

    TestEnvironment(int testCount) {
        this.maxTestCount = testCount;
        semaphore = new Semaphore(testCount);
        semaphore.drainPermits();
        MinecraftServer server = MinecraftServer.init/*Isolated*/();

        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            MiniClient client = clients.get(event.getPlayer().getUsername());
            assert client != null;
            client.confirmConnection();

            final Player player = event.getPlayer();
            event.setSpawningInstance(container);
            System.out.println("Player "+player.getUsername()+" connected.");
        });

        server.start("localhost", 25565); // TODO: random port?
    }

    public MiniClient newClient() {
        String testName = "TODO-"+counter.getAndIncrement();
        MiniClient client = new MiniClient(testName);
        clients.put(testName, client);

        // ensure client is properly connected to server before handing back control flow to test code
        waitForConnection(client);
        return client;
    }

    private void waitForConnection(MiniClient client) {
        while(!client.getConnectionConfirmed()) {
            Thread.yield();
        }
    }

    public void waitNetworkIdle() {

    }

    @Override
    public void close() throws IOException {
        semaphore.release();
        for(MiniClient client : clients.values()) {
            client.stop();
        }
        clients.clear();

        // stop server when all tests using this environment are done
        if(semaphore.availablePermits() == maxTestCount) {
            MinecraftServer.stopCleanly();
        }
    }

    public void waitTime(long amount, TimeUnit unit) throws InterruptedException {
        var latch = new CountDownLatch(1);
        MinecraftServer.getSchedulerManager().buildTask(latch::countDown).delay(amount, unit).schedule();

        latch.await();
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
