package net.minestom.server.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.location.RelativeVec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTest {

    @Test
    public void testParseSelf() {
        assertEquals("example", Argument.parse(new ServerSender(), ArgumentType.String("example")));
        assertEquals(55, Argument.parse(new ServerSender(), ArgumentType.Integer("55")));
    }

    @Test
    public void testCallback() {
        var arg = ArgumentType.String("id");

        assertFalse(arg.hasErrorCallback());
        arg.setCallback((sender, exception) -> {
        });
        assertTrue(arg.hasErrorCallback());
    }

    @Test
    public void testDefaultValue() {
        var arg = ArgumentType.String("id");

        assertFalse(arg.isOptional());
        arg.setDefaultValue("default value");
        assertTrue(arg.isOptional());
        assertEquals("default value", arg.getDefaultValue().apply(new ServerSender()));
    }

    @Test
    public void testSuggestionCallback() {
        var arg = ArgumentType.String("id");

        assertFalse(arg.hasSuggestion());

        arg.setSuggestionCallback((sender, context, suggestion) -> suggestion.addEntry(new SuggestionEntry("entry")));
        assertTrue(arg.hasSuggestion());

        Suggestion suggestion = new Suggestion("input", 2, 4);
        arg.getSuggestionCallback().apply(new ServerSender(), new CommandContext("input"), suggestion);

        assertEquals(suggestion.getEntries(), List.of(new SuggestionEntry("entry")));
    }

    @Test
    public void testArgumentCallbackExecution() {
        MinecraftServer.init();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        List<String> executedCallbacks = new ArrayList<>();

        Command test = new Command("test");

        ArgumentCallback defaultNumberCallback = (sender, exception) -> {
            executedCallbacks.add("defaultNumberCallback");
        };
        Argument<Integer> integer = ArgumentType.Integer("number");
        integer.setCallback(defaultNumberCallback);

        // /test <integer>
        test.addSyntax((sender, context) -> {}, integer);

        Argument<RelativeVec> vec = ArgumentType.RelativeVec3("vec");
        // /test <vec>
        test.addSyntax((sender, context) -> {}, vec);

        Argument<Integer> firstInt = ArgumentType.Integer("firstInt");
        firstInt.setCallback(defaultNumberCallback);
        Argument<Integer> secondInt = ArgumentType.Integer("secondInt");
        secondInt.setCallback(defaultNumberCallback);

        // /test <integer> <integer>
        test.addSyntax((sender, context) -> {}, firstInt, secondInt);

        test.setFallbackArgumentCallback((sender, exception) -> {
            executedCallbacks.add("fallbackArgumentCallback");
        });

        // /test <integer> -> fallback argument callback should be executed if the 1st argument cannot be parsed
        //       <vec> -> same as <integer>
        //       <firstInt> <secondInt> -> <firstInt> has the same behaviour of <integer>, but if the second argument cannot be parsed,
        //                                 its argument callback will be used
        //                                 because there's no other parser that attempts to parse the second argument input

        commandManager.register(test);

        commandManager.executeServerCommand("test text");
        assertTrue(executedCallbacks.contains("fallbackArgumentCallback"));
        commandManager.executeServerCommand("test 1 text");
        assertTrue(executedCallbacks.contains("defaultNumberCallback"));
    }
}