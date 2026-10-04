package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

@RegistriesTest
public class PacketPoolRegistriesTest {

    @Test
    public void packetPoolClearsRegistryContext(Registries registries) {
        final var pool = PacketVanilla.PACKET_POOL;
        pool.clear();

        final NetworkBuffer buffer = pool.get();
        buffer.registries(registries);
        pool.add(buffer);

        final NetworkBuffer recycled = pool.get();
        try {
            assertNull(recycled.registries());
        } finally {
            pool.add(recycled);
            pool.clear();
        }
    }
}
