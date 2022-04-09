package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

/**
 * Holds the data of an invalidated syntax.
 */
public record CommandSuggestionHolder(CommandSyntax syntax,
                                      ArgumentSyntaxException argumentSyntaxException,
                                      int argIndex) {
}
