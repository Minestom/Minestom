package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerTickEvent;
import org.junit.jupiter.api.Test;

@EnvTest
public class InstanceUnregisterIntegrationTest {

    @Test
    public void sharedInstance(Env env) {
        // Ensure that unregistering a shared instance does not unload the container chunks
        var instanceManager = env.process().instance();
        var instance = instanceManager.createInstanceContainer();
        var shared1 = instanceManager.createSharedInstance(instance);
        var connection = env.createConnection();
        var player = connection.connect(shared1, new Pos(0, 40, 0)).join();

        var listener = env.listen(PlayerTickEvent.class);
        listener.followup();
        env.tick();

        player.setInstance(instanceManager.createSharedInstance(instance)).join();
        listener.followup();
        env.tick();

        instanceManager.unregisterInstance(shared1);
        listener.followup();
        env.tick();
    }
}
