package net.minestom.server.command;

final class CommandStringReader {
    static final char SPACE = ' ';
    private final String input;
    private int cursor = 0;

    CommandStringReader(String input) {
        this.input = input;
    }

    boolean hasRemaining() {
        return input.length() - cursor > 0;
    }

    String readWord() {
        final String input = this.input;
        final int cursor = this.cursor;

        final int i = input.indexOf(SPACE, cursor);
        if (i == -1) {
            this.cursor = input.length() + 1;
            return input.substring(cursor);
        }
        final String read = input.substring(cursor, i);
        this.cursor += read.length() + 1;
        return read;
    }

    String readRemaining() {
        final String input = this.input;
        final String result = input.substring(cursor);
        this.cursor = input.length();
        return result;
    }

    int cursor() {
        return cursor;
    }

    void cursor(int cursor) {
        this.cursor = cursor;
    }
}
