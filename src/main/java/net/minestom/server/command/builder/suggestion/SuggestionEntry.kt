package net.minestom.server.command.builder.suggestion

import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.command.builder.CommandContext

class SuggestionEntry @JvmOverloads constructor(val entry: String, val tooltip: Component? = null)