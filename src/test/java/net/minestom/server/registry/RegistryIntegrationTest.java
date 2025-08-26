package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@EnvTest
public class RegistryIntegrationTest {

    @Test
    void testUnnamedPack(Env env) {
        DynamicRegistry<DimensionType> dimensionRegistry = env.process().dimensionType();
        DimensionType dimensionType = DimensionType.builder()
                .ambientLight(2f)
                .build();
        var registryKey = dimensionRegistry.register(Key.key("toocool:fortests"), dimensionType, DataPack.MINESTOM_UNNAMED);
        assertEquals(dimensionType, dimensionRegistry.get(registryKey));
        assertEquals(DataPack.MINESTOM_UNNAMED, dimensionRegistry.getPack(registryKey));
        assertDoesNotThrow(() -> {
            dimensionRegistry.registryDataPacket(env.process(), false);
        }, "Registry data packet should not throw for null pack");
    }

    @Test
    void testDifferentPacksInterlaced(Env env) {
        DynamicRegistry<DimensionType> dimensionRegistry = env.process().dimensionType();
        DimensionType dimensionType = DimensionType.builder()
                .ambientLight(2f)
                .build();
        assertDoesNotThrow(()-> dimensionRegistry.register(Key.key("toocool:fortests"), dimensionType, DataPack.MINESTOM_UNNAMED));
        assertDoesNotThrow(() -> dimensionRegistry.register(Key.key("toocool:fortests2"), dimensionType, DataPack.MINECRAFT_CORE));
    }
}
