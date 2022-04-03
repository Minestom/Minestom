package net.minestom.server.command.builder.parser

import net.minestom.server.command.builder.arguments.Argument.id
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity.singleEntity
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity.onlyPlayers
import net.minestom.server.command.builder.arguments.Argument.useRemaining
import net.minestom.server.command.builder.arguments.Argument.parse
import net.minestom.server.command.builder.arguments.Argument.allowSpace
import net.minestom.server.command.builder.CommandDispatcher
import net.minestom.server.command.builder.parser.CommandQueryResult
import net.minestom.server.command.builder.parser.CommandParser
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.parser.ValidSyntaxHolder
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap
import net.minestom.server.command.builder.parser.CommandSuggestionHolder
import net.minestom.server.command.builder.parser.ArgumentParser.ArgumentResult
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.parser.ArgumentQueryResult
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import java.lang.StringBuilder
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import java.util.Locale
import java.lang.IllegalArgumentException
import java.util.function.IntFunction
import net.minestom.server.command.builder.exception.ArgumentSyntaxException

class ArgumentQueryResult 