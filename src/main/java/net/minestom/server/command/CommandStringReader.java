package net.minestom.server.command;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

interface CommandStringReader {
    @Contract(value = "_ -> new", pure = true)
    static @NotNull CommandStringReader from(CharSequence input) {
        return new CommandStringReaderImpl(input);
    }

    boolean hasRemaining();

    String readWord();

    String readQuotedString();

    String readRemaining();

    char peekNextChar();

    char getCharAt(int position);

    int cursor();

    void setCursor(int cursor);

    String readUntil(char c);

    String readUntilAny(char... c);

    default String readQuotablePhrase() {
        final char c = peekNextChar();
        if (c == '"' || c == '\'') {
            return readQuotedString();
        } else {
            return readWord();
        }
    }
}
