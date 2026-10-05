package net.minestom.server.instance;

import net.minestom.server.registry.Registries;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InstanceManagerIntegrationTest {

    @Test
    public void createInstanceContainerUsesGivenRegistries(Env env) {
        final InstanceManager manager = env.process().instance();
        final Registries registries = Registries.vanilla();
        assertNotSame(env.process().registries(), registries);

        final InstanceContainer instance = manager.createInstanceContainer(registries, DimensionType.OVERWORLD);
        final InstanceContainer withLoader = manager.createInstanceContainer(registries, DimensionType.OVERWORLD, null);

        assertSame(registries, instance.registries());
        assertSame(registries, withLoader.registries());
        assertTrue(manager.getInstances().contains(instance));
        assertTrue(manager.getInstances().contains(withLoader));
    }
}
