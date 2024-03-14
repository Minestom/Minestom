package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.permission.PermissionHandlerImpl;
import net.minestom.server.permission.PermissionHandlerProxy;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CommandSenderTest {

    @Test
    public void testMessageSending() {
        SenderTest sender = new SenderTest();

        assertNull(sender.getMostRecentMessage());

        sender.sendMessage("Hey!!");
        assertEquals(sender.getMostRecentMessage(), Component.text("Hey!!"));

        sender.sendMessage(new String[]{"Message", "Sending", "Test"});
        assertEquals(sender.getMostRecentMessage(), Component.text("Test"));

        sender.sendMessage(Component.text("Message test!", NamedTextColor.GREEN));
        assertEquals(sender.getMostRecentMessage(), Component.text("Message test!", NamedTextColor.GREEN));
    }

    private static final class SenderTest implements CommandSender, PermissionHandlerProxy {

        private final PermissionHandler permissions = new PermissionHandlerImpl();
        private final TagHandler handler = TagHandler.newHandler();

        private Component mostRecentMessage = null;

        @Override
        public @NotNull PermissionHandler getPermissionHandler() {
            return permissions;
        }

        @Override
        public @NotNull TagHandler tagHandler() {
            return handler;
        }

        @Override
        public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
            mostRecentMessage = message;
        }

        public @Nullable Component getMostRecentMessage() {
            return mostRecentMessage;
        }

        @Override
        public @NotNull Identity identity() {
            return Identity.nil();
        }
    }
}