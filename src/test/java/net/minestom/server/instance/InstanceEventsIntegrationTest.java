package net.minestom.server.instance;

import net.minestom.server.event.instance.InstanceRegisterEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
public class InstanceEventsIntegrationTest {
    @Test
    public void registerAndUnregisterInstance(Env env) {
        var registerListener = env.listen(InstanceRegisterEvent.class);
        var unregisterListener = env.listen(InstanceUnregisterEvent.class);

        registerListener.followup();
        Instance instance = env.process().instance().createInstanceContainer();

        unregisterListener.followup();
        env.destroyInstance(instance);
    }
}
