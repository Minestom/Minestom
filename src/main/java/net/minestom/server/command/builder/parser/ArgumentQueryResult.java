package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;

public record ArgumentQueryResult(CommandSyntax syntax,
                                  Argument<?> argument,
                                  CommandContext context,
                                  String input) {
}
