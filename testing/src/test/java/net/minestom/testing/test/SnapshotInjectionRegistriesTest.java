package net.minestom.testing.test;

import net.minestom.server.registry.Registries;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@RegistriesTest
class SnapshotInjectionRegistriesTest {
    private static Registries lifecycleRegistries;

    @BeforeAll
    static void captureRegistries(Registries registries) {
        lifecycleRegistries = registries;
    }

    @Test
    void injectsRegistries(Registries registries) {
        assertNotNull(registries.biome());
        assertSame(lifecycleRegistries, registries);
    }
}
