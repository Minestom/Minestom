package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;

public class ArgumentQueryResult {
    public CommandSyntax syntax;
    public Argument<?> argument;
    public CommandContext context;
    public String input;
}
