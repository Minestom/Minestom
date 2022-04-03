package net.minestom.server.command.builder.suggestion

import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.command.builder.CommandContext
import java.util.ArrayList

class Suggestion(val input: String, var start: Int, var length: Int) {
    private val suggestionEntries: MutableList<SuggestionEntry> = ArrayList()
    val entries: List<SuggestionEntry>
        get() = suggestionEntries

    fun addEntry(entry: SuggestionEntry) {
        suggestionEntries.add(entry)
    }
}