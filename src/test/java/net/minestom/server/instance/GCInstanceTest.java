package net.minestom.server.instance;

import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.UUID;

import static net.minestom.testing.TestUtils.waitUntilCleared;

@EnvTest
public class GCInstanceTest {
    @Test
    public void testGCAfterUnregister(Env env) {
        var ref = new WeakReference<>(new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD));
        env.process().instance().registerInstance(ref.get());
        env.process().instance().unregisterInstance(ref.get());

        waitUntilCleared(ref);
    }

    @Test
    public void testGCWithEventsLambda(Env env) {
        var ref = new WeakReference<>(new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD));
        env.process().instance().registerInstance(ref.get());
        registerEvents(ref.get());
        ref.get().tick(0);
        env.process().instance().unregisterInstance(ref.get());

        waitUntilCleared(ref);
    }

    private void registerEvents(Instance instanceContainer) {
        instanceContainer.eventNode().addListener(InstanceTickEvent.class, (e) -> {
            System.out.println("Registered " + instanceContainer.getUniqueId());
        });
    }
}
