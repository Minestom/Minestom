package net.minestom.server.particle.shapes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LinePattern {
    private final boolean[] pattern;

    private LinePattern(boolean[] pattern) {
        this.pattern = pattern;
    }

    private LinePattern(@Nullable String pattern) {
        this.pattern = createPattern(pattern);
    }

    public @NotNull LinePattern withOffset(int offset) {
        boolean[] result = new boolean[pattern.length];

        System.arraycopy(pattern, 0, result, offset, pattern.length - offset);
        System.arraycopy(pattern, pattern.length - offset, result, 0, offset);

        return new LinePattern(result);
    }

    public @NotNull LinePattern.Iterator iterator() {
        return new Iterator(this);
    }

    private boolean[] createPattern(@Nullable String pattern) {
        if (pattern == null || pattern.length() == 0) {
            return new boolean[] {true};
        }

        boolean[] result = new boolean[pattern.length()];

        char[] charArray = pattern.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            result[i] = !Character.isSpaceChar(c);
        }

        return result;
    }

    public static class Iterator {
        private final LinePattern linePattern;
        private int index = 0;

        private Iterator(LinePattern linePattern) {
            this.linePattern = linePattern;
        }

        public boolean next() {
            if (index >= linePattern.pattern.length) {
                index = 0;
            }

            return linePattern.pattern[index++];
        }

        public Iterator reset() {
            index = 0;
            return this;
        }
    }

    public static LinePattern of(@Nullable String pattern) {
        return new LinePattern(pattern);
    }

    public static LinePattern empty() {
        return new LinePattern((String) null);
    }
}
