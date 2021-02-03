package improveextensions.unloadcallbacks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

public class UnloadCallbacksExtension extends Extension {

    private boolean ticked = false;
    private final EventCallback<InstanceTickEvent> callback = this::onTick;

    private void onTick(InstanceTickEvent e) {
        ticked = true;
    }

    @Override
    public void initialize() {
        GlobalEventHandler globalEvents = MinecraftServer.getGlobalEventHandler();
        // this callback will be automatically removed when unloading the extension
        globalEvents.addEventCallback(InstanceTickEvent.class, callback);

        try {
            Assertions.assertTrue(EventHandler.getExtensionOwningCallback(callback).isPresent());
            Assertions.assertEquals("UnloadCallbacksExtension", EventHandler.getExtensionOwningCallback(callback).get());
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
        ticked = false;

        // TODO: set to transient task to avoid losing the task on termination
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            // Make sure callback is disabled
            try {
                Assertions.assertFalse(ticked, "ticked should be false because the callback has been unloaded");
            } catch (AssertionFailedError e) {
                e.printStackTrace();
            }
            MinecraftServer.stopCleanly();
        }).delay(1L, TimeUnit.SECOND).schedule();
    }
}
