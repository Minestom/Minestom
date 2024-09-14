package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket;
import net.minestom.server.utils.NamespaceID;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateEnabledFeaturesPacketTest {

    @Test
    void constructorWithBufferReadsCorrectly() {
        NetworkBuffer buffer = new NetworkBuffer();
        UpdateEnabledFeaturesPacket updateEnabledFeaturesPacket = new UpdateEnabledFeaturesPacket(Set.of(NamespaceID.from("namespace:feature1"), NamespaceID.from("namespace:feature2")));
        updateEnabledFeaturesPacket.write(buffer);
        UpdateEnabledFeaturesPacket packet = new UpdateEnabledFeaturesPacket(buffer);

        assertEquals(2, packet.features().size());
        assertTrue(packet.features().contains(NamespaceID.from("namespace:feature1")));
        assertTrue(packet.features().contains(NamespaceID.from("namespace:feature2")));
    }


    @Test
    void configurationIdReturnsCorrectId() {
        UpdateEnabledFeaturesPacket packet = new UpdateEnabledFeaturesPacket(Set.of());
        assertEquals(ServerPacketIdentifier.CONFIGURATION_UPDATE_ENABLED_FEATURES, packet.configurationId());
    }

    @Test
    void constructorWithBufferHandlesEmptySet() {
        NetworkBuffer buffer = new NetworkBuffer();
        UpdateEnabledFeaturesPacket updateEnabledFeaturesPacket = new UpdateEnabledFeaturesPacket(Set.of());
        updateEnabledFeaturesPacket.write(buffer);

        UpdateEnabledFeaturesPacket packet = new UpdateEnabledFeaturesPacket(buffer);

        assertTrue(packet.features().isEmpty());
    }

    @Test
    void constructorWithBufferHandlesMaxFeatures() {
        Set<NamespaceID> maxFeatures = new HashSet<>();
        for (int i = 0; i < UpdateEnabledFeaturesPacket.MAX_FEATURES; i++) {
            maxFeatures.add(NamespaceID.from("namespace:feature" + i));
        }
        NetworkBuffer buffer = new NetworkBuffer();
        UpdateEnabledFeaturesPacket updateEnabledFeaturesPacket = new UpdateEnabledFeaturesPacket(maxFeatures);
        updateEnabledFeaturesPacket.write(buffer);

        UpdateEnabledFeaturesPacket packet = new UpdateEnabledFeaturesPacket(buffer);

        assertEquals(UpdateEnabledFeaturesPacket.MAX_FEATURES, packet.features().size());
    }
}