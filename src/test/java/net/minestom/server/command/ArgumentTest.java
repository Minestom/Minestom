package net.minestom.server.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTest {

    @Test
    public void testParseSelf() {
        assertEquals("example", Argument.parse(ArgumentType.String("example")));
        assertEquals(55, Argument.parse(ArgumentType.Integer("55")));
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
        assertEquals("default value", arg.getDefaultValue().get());
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
}