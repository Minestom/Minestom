package net.minestom.server.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PacketUtilsTest {

    @Test
    public void testShouldUseCachePacketWithTranslatableComponent() {
        final var packet = new ChatMessagePacket(Component.translatable("test.key"), ChatPosition.CHAT, UUID.randomUUID());

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = false;
        assertTrue(PacketUtils.shouldUseCachePacket(packet));

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        assertFalse(PacketUtils.shouldUseCachePacket(packet));
    }

    @Test
    public void testShouldUseCachePacketWithTextComponent() {
        final var packet = new ChatMessagePacket(Component.text("This is a test"), ChatPosition.CHAT, UUID.randomUUID());

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        assertTrue(PacketUtils.shouldUseCachePacket(packet));

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = false;
        assertTrue(PacketUtils.shouldUseCachePacket(packet));
    }

}
