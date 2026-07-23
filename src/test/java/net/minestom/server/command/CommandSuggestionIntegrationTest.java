package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class CommandSuggestionIntegrationTest {

    @Test
    public void suggestion(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("test");
        command.addSyntax((_, _) -> {

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
                (_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("suggestion"))
        );
        var defaultArg = Integer("defaultArg").setDefaultValue(123);

        var command = new Command("foo");

        command.addSyntax((_,_)->{}, suggestArg, defaultArg);
        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(1, "foo 1"));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> assertEquals(List.of(new TabCompletePacket.Match("suggestion", null)), tabCompletePacket.matches()));
    }

    @Test
    public void suggestionWithSubcommand(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        var subCommand = new Command("bar");

        var wordArg1 = Word("wordArg1").setSuggestionCallback((_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("suggestionA")));
        var wordArg2 = Word("wordArg2").setSuggestionCallback((_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("suggestionB")));

        subCommand.addSyntax((_, _) -> {}, wordArg1, wordArg2);

        command.addSyntax((_,_)->{}, Literal("literal"), wordArg2);

        command.addSubcommand(subCommand);

        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(1, "foo bar "));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> assertEquals(List.of(new TabCompletePacket.Match("suggestionA", null)), tabCompletePacket.matches()));
    }

    /**
     * Verifies that a suggestion callback fires when the first argument rejects the placeholder word
     * (i.e. the argument uses semantic validation that throws ArgumentSyntaxException for unknown values).
     * Without proper suggestion-callback propagation the suggestion would be silently dropped because
     * parseArgument() turns ArgumentSyntaxException into IncompatibleType, and lastSuccessfulResult()
     * then walks back to a chain that never had the callback attached.
     */
    @Test
    public void suggestionWithValidatingArgument(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        // from() makes the argument reject anything not in the list — same failure mode as a
        // custom parser that throws ArgumentSyntaxException for unknown semantic values.
        var validatingArg = Word("validatingArg")
                .from("option1", "option2")
                .setSuggestionCallback((sender, context, suggestion) -> {
                    suggestion.addEntry(new SuggestionEntry("option1"));
                    suggestion.addEntry(new SuggestionEntry("option2"));
                });

        command.addSyntax((sender, context) -> {}, validatingArg);
        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(1, "foo "));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> {
            assertEquals(List.of(
                    new TabCompletePacket.Match("option1", null),
                    new TabCompletePacket.Match("option2", null)
            ), tabCompletePacket.matches());
        });
    }

    /**
     * Verifies that a suggestion callback fires when the first argument rejects the placeholder word
     * (i.e. the argument uses semantic validation that throws ArgumentSyntaxException for unknown values).
     * Without proper suggestion-callback propagation the suggestion would be silently dropped because
     * parseArgument() turns ArgumentSyntaxException into IncompatibleType, and lastSuccessfulResult()
     * then walks back to a chain that never had the callback attached.
     */
    @Test
    public void suggestionWithValidatingArgument(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        // from() makes the argument reject anything not in the list — same failure mode as a
        // custom parser that throws ArgumentSyntaxException for unknown semantic values.
        var validatingArg = Word("validatingArg")
                .from("option1", "option2")
                .setSuggestionCallback((sender, context, suggestion) -> {
                    suggestion.addEntry(new SuggestionEntry("option1"));
                    suggestion.addEntry(new SuggestionEntry("option2"));
                });

        command.addSyntax((sender, context) -> {}, validatingArg);
        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(1, "foo "));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> {
            assertEquals(List.of(
                    new TabCompletePacket.Match("option1", null),
                    new TabCompletePacket.Match("option2", null)
            ), tabCompletePacket.matches());
        });
    }

    @Test
    public void suggestionWithTwoLiterals(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var command = new Command("foo");

        var wordArg1 = Word("wordArg1").setSuggestionCallback((_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("suggestionA")));
        var wordArg2 = Word("wordArg2").setSuggestionCallback((_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("suggestionB")));

        command.addSyntax((_,_)->{}, Literal("literal1"), wordArg1);

        command.addSyntax((_,_)->{}, Literal("literal2"), wordArg2);

        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(1, "foo literal2 "));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> assertEquals(List.of(new TabCompletePacket.Match("suggestionB", null)), tabCompletePacket.matches()));
    }
}
