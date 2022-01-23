package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.Command;

import java.util.List;

public record CommandQueryResult(List<Command> parents,
                                 Command command,
                                 String commandName,
                                 String[] args) {
}
