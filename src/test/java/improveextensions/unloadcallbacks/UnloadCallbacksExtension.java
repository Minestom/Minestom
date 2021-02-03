package improveextensions.unloadcallbacks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

public class UnloadCallbacksExtension extends Extension {

    private boolean ticked1 = false;
    private boolean ticked2 = false;
    private boolean tickedScheduledNonTransient = false;
    private boolean tickedScheduledTransient = false;
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

        // this callback will be cancelled
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            tickedScheduledNonTransient = true;
        }).repeat(100L, TimeUnit.MILLISECOND).schedule();

        // this callback will NOT be cancelled
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            tickedScheduledTransient = true;
        }).repeat(100L, TimeUnit.MILLISECOND).makeTransient().schedule();

        try {
            Assertions.assertTrue(MinestomRootClassLoader.findExtensionObjectOwner(callback).isPresent());
            Assertions.assertEquals("UnloadCallbacksExtension", MinestomRootClassLoader.findExtensionObjectOwner(callback).get());
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
        ticked1 = false;
        ticked2 = false;
        tickedScheduledNonTransient = false;
        tickedScheduledTransient = false;

        // TODO: set to transient task to avoid losing the task on termination
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            // Make sure callback is disabled
            try {
                Assertions.assertFalse(ticked1, "ticked1 should be false because the callback has been unloaded");
                Assertions.assertFalse(ticked2, "ticked2 should be false because the callback has been unloaded");
                Assertions.assertFalse(tickedScheduledNonTransient, "tickedScheduledNonTransient should be false because the callback has been unloaded");
                Assertions.assertTrue(tickedScheduledTransient, "tickedScheduledNonTransient should be true because the callback has NOT been unloaded");
            } catch (AssertionFailedError e) {
                e.printStackTrace();
            }
            MinecraftServer.stopCleanly();
        }).delay(1L, TimeUnit.SECOND).makeTransient().schedule();
    }
}
