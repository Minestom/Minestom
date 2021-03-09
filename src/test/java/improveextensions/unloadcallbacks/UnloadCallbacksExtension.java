package improveextensions.unloadcallbacks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicBoolean;

public class UnloadCallbacksExtension extends Extension {

    private boolean ticked1 = false;
    private boolean ticked2 = false;
    private boolean tickedScheduledNonTransient = false;
    private boolean tickedScheduledTransient = false;
    private boolean zombieTicked = false;
    private boolean instanceTicked = false;
    private final EventCallback<InstanceTickEvent> callback = this::onTick;

    private void onTick(InstanceTickEvent e) {
        ticked1 = true;
    }

    @Override
    public void initialize() {
        GlobalEventHandler globalEvents = MinecraftServer.getGlobalEventHandler();
        // this callback will be automatically removed when unloading the extension
        globalEvents.addEventCallback(InstanceTickEvent.class, callback);
        // this one too
        globalEvents.addEventCallback(InstanceTickEvent.class, e -> ticked2 = true);

        Instance instance = MinecraftServer.getInstanceManager().getInstances().stream().findFirst().orElseThrow();

        // add an event callback on an instance
        instance.addEventCallback(InstanceTickEvent.class, e -> instanceTicked = true);
        instance.loadChunk(0, 0);

        // add an event callback on an entity
        EntityCreature zombie = new EntityCreature(EntityType.ZOMBIE);
        zombie.addEventCallback(EntityTickEvent.class, e -> {
            zombieTicked = true;
        });
        zombie.setInstance(instance, new Position(8, 64, 8) /* middle of chunk */);

        // this callback will be cancelled
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            tickedScheduledNonTransient = true;
        }).repeat(100L, TimeUnit.MILLISECOND).schedule();

        // this callback will NOT be cancelled
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            tickedScheduledTransient = true;
        }).repeat(100L, TimeUnit.MILLISECOND).makeTransient().schedule();

        try {
            Assertions.assertNotNull(MinestomRootClassLoader.findExtensionObjectOwner(callback));
            Assertions.assertEquals("UnloadCallbacksExtension", MinestomRootClassLoader.findExtensionObjectOwner(callback));
        } catch (AssertionFailedError e) {
            e.printStackTrace();
            System.exit(-1);
        }

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            // unload self
            MinecraftServer.getExtensionManager().unloadExtension(getDescription().getName());
        }).delay(1L, TimeUnit.SECOND).schedule();
    }

    @Override
    public void terminate() {
        new Thread(() -> {
            try {
                // wait for complete termination of this extension
                Thread.sleep(10);
                ticked1 = false;
                ticked2 = false;
                tickedScheduledNonTransient = false;
                tickedScheduledTransient = false;
                instanceTicked = false;
                zombieTicked = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        AtomicBoolean executedDelayTaskAfterTerminate = new AtomicBoolean(false);
        // because terminate is called just before unscheduling and removing event callbacks,
        //  the following task will never be executed, because it is not transient
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            executedDelayTaskAfterTerminate.set(true);
        }).delay(100L, TimeUnit.MILLISECOND).schedule();

        // this shutdown tasks will not be executed because it is not transient
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> Assertions.fail("This shutdown task should be unloaded when the extension is")).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            // Make sure callbacks are disabled
            try {
                Assertions.assertFalse(ticked1, "ticked1 should be false because the callback has been unloaded");
                Assertions.assertFalse(ticked2, "ticked2 should be false because the callback has been unloaded");
                Assertions.assertFalse(tickedScheduledNonTransient, "tickedScheduledNonTransient should be false because the callback has been unloaded");
                Assertions.assertFalse(zombieTicked, "zombieTicked should be false because the callback has been unloaded");
                Assertions.assertFalse(instanceTicked, "instanceTicked should be false because the callback has been unloaded");
                Assertions.assertTrue(tickedScheduledTransient, "tickedScheduledNonTransient should be true because the callback has NOT been unloaded");
                Assertions.assertFalse(executedDelayTaskAfterTerminate.get(), "executedDelayTaskAfterTerminate should be false because the callback has been unloaded before executing");
                System.out.println("All tests passed.");
            } catch (AssertionFailedError e) {
                e.printStackTrace();
            }
            MinecraftServer.stopCleanly(); // TODO: fix deadlock which happens because stopCleanly waits on completion of scheduler tasks
        }).delay(1L, TimeUnit.SECOND).makeTransient().schedule();
    }
}
