package net.minestom.server.command.builder.suggestion

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.command.builder.CommandContext

fun interface SuggestionCallback {
    fun apply(sender: CommandSender, context: CommandContext, suggestion: Suggestion)
}