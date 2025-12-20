package net.minestom.server.entity;

import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityVillagerMetaTest {
    @Test
    void levelNetworkSerialization() {
        NetworkBuffer buffer = NetworkBuffer.builder(5).build();
        VillagerMeta.Level.NETWORK_TYPE.write(buffer, VillagerMeta.Level.NOVICE);

        int expected = VillagerMeta.Level.NOVICE.ordinal() + 1;  // Network representation is ordinal + 1
        int readValue = buffer.read(NetworkBuffer.VAR_INT);
        assertEquals(expected, readValue);
    }

    @Test
    void levelNetworkDeserialization() {
        int networkValue = VillagerMeta.Level.NOVICE.ordinal() + 1;  // Simulate network value for NOVICE
        NetworkBuffer buffer = NetworkBuffer.builder(5).build();
        buffer.write(NetworkBuffer.VAR_INT, networkValue);

        VillagerMeta.Level level = VillagerMeta.Level.NETWORK_TYPE.read(buffer);
        assertEquals(VillagerMeta.Level.NOVICE, level);
    }
}
