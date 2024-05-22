package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CommandManagerTest {

    @Test
    void testCommandRegistration() {
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
    void testUnknownCommandCallback() {
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
    void testSharedArgumentSyntaxABFirst() {
        var manager = new CommandManager();

        var checkA = new AtomicBoolean(false);
        var checkAB = new AtomicBoolean(false);

        var cmd = new Command("cmd");
        var argA = ArgumentType.String("a");
        var argB = ArgumentType.String("b");
        cmd.addSyntax((sender, context) -> checkAB.set(true), argA, argB);
        cmd.addSyntax((sender, context) -> checkA.set(true), argA);
        manager.register(cmd);

        var result = manager.executeServerCommand("cmd a");
        assertEquals(CommandResult.Type.SUCCESS, result.getType());
        assertTrue(checkA.get());
        assertFalse(checkAB.get());

        checkA.set(false); // these should be different tests
        checkAB.set(false);

        result = manager.executeServerCommand("cmd a b");
        assertEquals(CommandResult.Type.SUCCESS, result.getType());
        assertFalse(checkA.get());
        assertTrue(checkAB.get());
    }

    @Test
    void testSharedArgumentSyntaxAFirst() {
        var manager = new CommandManager();

        var checkA = new AtomicBoolean(false);
        var checkAB = new AtomicBoolean(false);

        var cmd = new Command("cmd");
        var argA = ArgumentType.String("a");
        var argB = ArgumentType.String("b");
        cmd.addSyntax((sender, context) -> checkA.set(true), argA);
        cmd.addSyntax((sender, context) -> checkAB.set(true), argA, argB);
        manager.register(cmd);

        var result = manager.executeServerCommand("cmd a");
        assertEquals(CommandResult.Type.SUCCESS, result.getType());
        assertTrue(checkA.get());
        assertFalse(checkAB.get());

        checkA.set(false); // these should be different tests
        checkAB.set(false);

        result = manager.executeServerCommand("cmd a b");
        assertEquals(CommandResult.Type.SUCCESS, result.getType());
        assertFalse(checkA.get());
        assertTrue(checkAB.get());
    }

    private static void assertNodeEquals(DeclareCommandsPacket.Node node, byte flags, int[] children, int redirectedNode,
                                         String name, String parser, byte[] properties, String suggestionsType) {
        assertEquals(flags, node.flags);
        assertArrayEquals(children, node.children);
        assertEquals(redirectedNode, node.redirectedNode);
        assertEquals(name, node.name);
        assertEquals(parser, node.parser);
        assertArrayEquals(properties, node.properties);
        assertEquals(suggestionsType, node.suggestionsType);
    }

}
