package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
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

    @Test
    void applyRegistryDataPacket() {
        Registries source = Registries.vanilla();
        Registries target = Registries.vanilla();
        Key key = Key.key("minestom:test_dimension");
        DimensionType dimensionType = DimensionType.builder()
                .ambientLight(0.5f)
                .build();
        RegistryKey<DimensionType> sourceKey = source.dimensionType().register(key, dimensionType, DataPack.MINESTOM_UNNAMED);

        RegistryDataPacket packet = (RegistryDataPacket) source.dimensionType().registryDataPacket(source, false);
        Registries.applyRegistryDataPacket(target, packet);

        assertEquals(source.dimensionType().size(), target.dimensionType().size());
        assertEquals(source.dimensionType().getId(sourceKey), target.dimensionType().getId(RegistryKey.unsafeOf(key)));
        assertEquals(dimensionType, target.dimensionType().get(key));
    }

    @Test
    void applyRegistryDataPacketWithVanillaPlaceholders() {
        Registries source = Registries.vanilla();
        Registries target = Registries.vanilla();
        Key key = Key.key("minestom:test_dimension");
        DimensionType dimensionType = DimensionType.builder()
                .ambientLight(0.75f)
                .build();
        source.dimensionType().register(key, dimensionType, DataPack.MINESTOM_UNNAMED);

        RegistryDataPacket packet = (RegistryDataPacket) ((CachedPacket) source.dimensionType()
                .registryDataPacket(source, true)).packet(ConnectionState.CONFIGURATION);
        assertNull(packet.entries().getFirst().data());

        Registries.applyRegistryDataPacket(target, packet);

        assertEquals(source.dimensionType().size(), target.dimensionType().size());
        assertEquals(dimensionType, target.dimensionType().get(key));
        assertEquals(DataPack.MINECRAFT_CORE, target.dimensionType().getPack(0));
        assertEquals(DataPack.MINESTOM_UNNAMED, target.dimensionType().getPack(RegistryKey.unsafeOf(key)));
    }

    @Test
    void applyAllRegistryDataPackets() {
        Registries source = Registries.vanilla();
        Registries target = Registries.vanilla();

        for (var packet : Registries.registryDataPackets(source, false)) {
            Registries.applyRegistryDataPacket(target, (RegistryDataPacket) packet);
        }

        assertEquals(source.chatType().size(), target.chatType().size());
        assertEquals(source.enchantment().size(), target.enchantment().size());
        assertEquals(source.dimensionType().size(), target.dimensionType().size());
    }
}
