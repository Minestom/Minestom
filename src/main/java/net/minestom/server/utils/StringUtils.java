package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

public class StringUtils {

    public static final String SPACE = " ";
    public static final char SPACE_CHAR = ' ';

    public static int countMatches(@NotNull final CharSequence str, final char ch) {
        if (str.length() == 0) {
            return 0;
        }
        int count = 0;
        // We could also call str.toCharArray() for faster look ups but that would generate more garbage.
        for (int i = 0; i < str.length(); i++) {
            if (ch == str.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Applies the Jaro-Winkler distance algorithm to the given strings, providing information about the
     * similarity of them.
     *
     * @param s1 The first string that gets compared. May be null or empty.
     * @param s2 The second string that gets compared. May be null or empty.
     * @return The Jaro-Winkler score (between 0.0 and 1.0), with a higher value indicating larger similarity.
     * @author Thomas Trojer thomas@trojer.net
     */
    public static double jaroWinklerScore(final String s1, final String s2) {
        // lowest score on empty strings
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
            return 0;
        }
        // highest score on equal strings
        if (s1.equals(s2)) {
            return 1;
        }
        // some score on different strings
        int prefixMatch = 0; // exact prefix matches
        int matches = 0; // matches (including prefix and ones requiring transpostion)
        int transpositions = 0; // matching characters that are not aligned but close together
        int maxLength = Math.max(s1.length(), s2.length());
        int maxMatchDistance = Math.max((int) Math.floor(maxLength / 2.0) - 1, 0); // look-ahead/-behind to limit transposed matches
        // comparison
        final String shorter = s1.length() < s2.length() ? s1 : s2;
        final String longer = s1.length() >= s2.length() ? s1 : s2;
        for (int i = 0; i < shorter.length(); i++) {
            // check for exact matches
            boolean match = shorter.charAt(i) == longer.charAt(i);
            if (match) {
                if (i < 4) {
                    // prefix match (of at most 4 characters, as described by the algorithm)
                    prefixMatch++;
                }
                matches++;
                continue;
            }
            // check fro transposed matches
            for (int j = Math.max(i - maxMatchDistance, 0); j < Math.min(i + maxMatchDistance, longer.length()); j++) {
                if (i == j) {
                    // case already covered
                    continue;
                }
                // transposition required to match?
                match = shorter.charAt(i) == longer.charAt(j);
                if (match) {
                    transpositions++;
                    break;
                }
            }
        }
        // any matching characters?
        if (matches == 0) {
            return 0;
        }
        // modify transpositions (according to the algorithm)
        transpositions = (int) (transpositions / 2.0);
        // non prefix-boosted score
        double score = 0.3334 * (matches / (double) longer.length() + matches / (double) shorter.length() + (matches - transpositions)
                / (double) matches);
        if (score < 0.7) {
            return score;
        }
        // we already have a good match, hence we boost the score proportional to the common prefix
        return score + prefixMatch * 0.1 * (1.0 - score);
    }

    public static String unescapeJavaString(String st) {
        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\' -> ch = '\\';
                    case 'b' -> ch = '\b';
                    case 'f' -> ch = '\f';
                    case 'n' -> ch = '\n';
                    case 'r' -> ch = '\r';
                    case 't' -> ch = '\t';
                    case '\"' -> ch = '\"';
                    case '\'' -> ch = '\'';

                    // Hex Unicode: u????
                    case 'u' -> {
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                    }
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
