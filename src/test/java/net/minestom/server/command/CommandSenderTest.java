package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CommandSenderTest {

    @Test
    public void testSenderPermissions() {

        CommandSender sender = new SenderTest();

        Permission permission = new Permission("permission.test", new NBTCompound());

        assertEquals(sender.getAllPermissions(), Set.of());

        sender.addPermission(permission);
        assertEquals(sender.getPermission(permission.getPermissionName()), permission);
        assertEquals(sender.getAllPermissions(), Set.of(permission));

        sender.removePermission(permission.getPermissionName());
        assertEquals(sender.getAllPermissions(), Set.of());
    }

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

    private static final class SenderTest implements CommandSender {

        private final Set<Permission> permissions = new HashSet<>();
        private final TagHandler handler = TagHandler.newHandler();

        private Component mostRecentMessage = null;

        @Override
        public @NotNull Set<Permission> getAllPermissions() {
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
    }
}
