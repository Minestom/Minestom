package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.listener.TabCompleteListener;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class CommandSuggestionIntegrationTest {

    @Test
    public void suggestionWithPackets(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("test");
        command.addSyntax((sender, context) -> {

        }, Literal("arg").setSuggestionCallback((sender, context, suggestion) -> {
            assertEquals(player, sender);
            assertEquals("test", context.getCommandName());
            assertEquals("test arg te", context.getInput());
            suggestion.addEntry(new SuggestionEntry("test1"));
        }));

        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(3, "test arg te"));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> {
            assertEquals(3, tabCompletePacket.transactionId());
            assertEquals(10, tabCompletePacket.start());
            assertEquals(2, tabCompletePacket.length());
            assertEquals(List.of(new TabCompletePacket.Match("test1", null)), tabCompletePacket.matches());
        });
    }

    @Test
    public void suggestionWithDefaults(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var suggestArg = Word("suggestArg").setSuggestionCallback(
                (sender, context, suggestion) -> suggestion.addEntry(new SuggestionEntry("suggestion"))
        );
        var defaultArg = Integer("defaultArg").setDefaultValue(123);

        var command = new Command("foo");

        command.addSyntax((sender,context)->{}, suggestArg, defaultArg);
        env.process().command().register(command);

        assertSameEntries("foo 1", List.of(new SuggestionEntry("suggestion")), player);
    }

    @Test
    public void suggestionWithSubcommand(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        var subCommand = new Command("bar");

        var wordArg1 = Word("wordArg1").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("suggestionA"));
        });
        var wordArg2 = Word("wordArg2").setSuggestionCallback((sender, context, suggestion) -> {
                    suggestion.addEntry(new SuggestionEntry("suggestionB"));
                });

        subCommand.addSyntax((sender, context) -> {}, wordArg1, wordArg2);

        command.addSyntax((sender,context)->{}, Literal("literal"), wordArg2);

        command.addSubcommand(subCommand);

        env.process().command().register(command);

        assertSameEntries("foo bar ", List.of(new SuggestionEntry("suggestionA")), player);
    }

    @Test
    public void suggestionWithTwoLiterals(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        var wordArg1 = Word("wordArg1").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("suggestionA"));
        });
        var wordArg2 = Word("wordArg2").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("suggestionB"));
        });

        command.addSyntax((sender,context)->{}, Literal("literal1"), wordArg1);

        command.addSyntax((sender,context)->{}, Literal("literal2"), wordArg2);

        env.process().command().register(command);

        assertSameEntries("foo literal2 ", List.of(new SuggestionEntry("suggestionB")), player);
    }

    @Test
    public void suggestionFromSingleSpaceSyntaxError(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        command.addSyntax((sender, context) -> {
        }, Integer("num").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("123456789"));
        }));

        env.process().command().register(command);

        assertSameEntries("foo ", List.of(new SuggestionEntry("123456789")), player);
    }

    @Test
    public void noSuggestionFromSyntaxError(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        command.addSyntax((sender, context) -> {
        }, Integer("num").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("123456789"));
        }));

        env.process().command().register(command);

        var entries = List.of(new SuggestionEntry("123456789"));

        assertNotSameEntries("foo  ", entries, player);
        assertNotSameEntries("foo bar", entries, player);
        assertNotSameEntries("foo 3ar", entries, player);
        assertNotSameEntries("foo 3ar ", entries, player);
        assertNotSameEntries("foo 3ar 2", entries, player);
        assertNotSameEntries("foo 3ar 2 ", entries, player);
    }

    private static void assertSameEntries(@NotNull String input, @NotNull List<SuggestionEntry> desired, @NotNull CommandSender sender) {
        Suggestion result = TabCompleteListener.getSuggestion(sender, input);
        assertNotNull(result, "Suggestion is null");
        assertEquals(desired, result.getEntries());
    }

    private static void assertNotSameEntries(@NotNull String input, @NotNull List<SuggestionEntry> undesired, @NotNull CommandSender sender) {
        Suggestion result = TabCompleteListener.getSuggestion(sender, input);
        if (result == null) {
            return;
        }
        assertNotEquals(result.getEntries(), undesired);
    }
}
