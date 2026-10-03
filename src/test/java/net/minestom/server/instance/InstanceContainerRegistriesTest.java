package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registries;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@RegistriesTest
public class InstanceContainerRegistriesTest {

    @Test
    public void copyPreservesTag(Registries registries) {
        var tag = Tag.String("test");
        var instance = new InstanceContainer(
                registries, UUID.randomUUID(),
                DimensionType.OVERWORLD, null, DimensionType.OVERWORLD.key());
        instance.setTag(tag, "123");

        var copyInstance = instance.copy();
        var result = copyInstance.getTag(tag);
        assertEquals("123", result);
    }

    @Test
    public void derivedInstancesPreserveRegistryContext(Registries registries) {
        final Key dimensionName = Key.key("minestom:derived");
        final InstanceContainer instance = new InstanceContainer(
                registries, UUID.randomUUID(),
                DimensionType.OVERWORLD, null, dimensionName);

        final InstanceContainer copy = instance.copy();
        final SharedInstance shared = new SharedInstance(UUID.randomUUID(), instance);

        assertSame(registries, copy.registries());
        assertSame(registries, shared.registries());
        assertEquals(dimensionName.asString(), copy.getDimensionName());
        assertEquals(dimensionName.asString(), shared.getDimensionName());
    }
}
