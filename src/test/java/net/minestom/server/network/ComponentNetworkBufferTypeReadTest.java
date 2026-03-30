package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.NBT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentNetworkBufferTypeReadTest {

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void networkBufferReadsShowEntityWithoutName() {
        UUID uuid = UUID.randomUUID();

        CompoundBinaryTag hoverEvent = CompoundBinaryTag.builder()
                .putString("action", "show_entity")
                .putString("id", "minecraft:player")
                .putString("uuid", uuid.toString())
                .build();

        CompoundBinaryTag component = CompoundBinaryTag.builder()
                .putString("type", "text")
                .putString("text", "hover")
                .put("hover_event", hoverEvent)
                .build();

        Component expected = Component.text("hover")
                .hoverEvent(HoverEvent.showEntity(
                        Key.key("minecraft:player"),
                        uuid,
                        null
                ));

        NetworkBuffer buffer = NetworkBuffer.resizableBuffer(256, MinecraftServer.process());
        buffer.write(NBT, component);
        buffer.readIndex(0);

        assertEquals(expected, buffer.read(COMPONENT));
    }
}