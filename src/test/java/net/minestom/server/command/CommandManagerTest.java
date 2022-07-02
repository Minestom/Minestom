package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CommandManagerTest {

    @Test
    public void testCommandRegistration() {
        var manager = new CommandManager();

        var command = new Command("name1", "name2");

        manager.register(command);

        assertTrue(manager.commandExists("name1"));
        assertTrue(manager.commandExists("name2"));
        assertFalse(manager.commandExists("name3"));

        manager.unregister(command);

        assertFalse(manager.commandExists("name1"));
        assertFalse(manager.commandExists("name2"));
        assertFalse(manager.commandExists("name3"));
    }

    @Test
    public void testUnknownCommandCallback() {
        var manager = new CommandManager();

        AtomicBoolean check = new AtomicBoolean(false);
        manager.setUnknownCommandCallback((sender, command) -> check.set(true));

        manager.register(new Command("valid_command"));

        manager.executeServerCommand("valid_command");
        assertFalse(check.get());

        manager.executeServerCommand("invalid_command");
        assertTrue(check.get());
    }

    @Test
    public void testDeclareCommandsPacket() {
        var manager = new CommandManager();

        var player = new Player(UUID.randomUUID(), "TestPlayer", null) {
            @Override
            protected void playerConnectionInit() {
            }

            @Override
            public boolean isOnline() {
                return false;
            }
        };

        manager.register(new Command("name"));

        var packet = manager.createDeclareCommandsPacket(player);

        assertEquals(packet.rootIndex(), 0);
        assertEquals(packet.nodes().size(), 2);
        assertNodeEquals(packet.nodes().get(0), (byte) 0, new int[]{1}, 0, "", "", null, "");
        assertNodeEquals(packet.nodes().get(1), (byte) 5, new int[0], 0, "name", "", null, "");
    }

    private static void assertNodeEquals(DeclareCommandsPacket.Node node, byte flags, int[] children, int redirectedNode,
                                         String name, String parser, byte[] properties, String suggestionsType) {
        assertEquals(node.flags, flags);
        assertArrayEquals(node.children, children);
        assertEquals(node.redirectedNode, redirectedNode);
        assertEquals(node.name, name);
        assertEquals(node.parser, parser);
        assertArrayEquals(node.properties, properties);
        assertEquals(node.suggestionsType, suggestionsType);
    }

}
