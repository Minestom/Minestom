package net.minestom.server.command.builder.suggestion;

import java.util.ArrayList;
import java.util.List;

public class Suggestion {

    private final String input;
    private int start;
    private int length;
    private final List<SuggestionEntry> suggestionEntries = new ArrayList<>();

    public Suggestion(String input, int start, int length) {
        this.input = input;
        this.start = start;
        this.length = length;
    }

    public String getInput() {
        return input;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<SuggestionEntry> getEntries() {
        return suggestionEntries;
    }

    public void addEntry(SuggestionEntry entry) {
        this.suggestionEntries.add(entry);
    }

}
