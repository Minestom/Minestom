package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;

import java.util.Map;

/**
 * Holds the data of a validated syntax.
 */
public record ValidSyntaxHolder(String commandString,
                                CommandSyntax syntax,
                                Map<Argument<?>, ArgumentParser.ArgumentResult> argumentResults) {

}
