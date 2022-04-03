package net.minestom.server.utils

import java.lang.StringBuilder
import net.minestom.server.utils.TickUtils
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.UniqueIdUtils

object StringUtils {
    const val SPACE = " "
    const val SPACE_CHAR = ' '
    @JvmStatic
    fun countMatches(str: CharSequence, ch: Char): Int {
        if (str.length == 0) {
            return 0
        }
        var count = 0
        // We could also call str.toCharArray() for faster look ups but that would generate more garbage.
        for (i in 0 until str.length) {
            if (ch == str[i]) {
                count++
            }
        }
        return count
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
    @JvmStatic
    fun jaroWinklerScore(s1: String?, s2: String?): Double {
        // lowest score on empty strings
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
            return 0
        }
        // highest score on equal strings
        if (s1 == s2) {
            return 1
        }
        // some score on different strings
        var prefixMatch = 0 // exact prefix matches
        var matches = 0 // matches (including prefix and ones requiring transpostion)
        var transpositions = 0 // matching characters that are not aligned but close together
        val maxLength = Math.max(s1.length, s2.length)
        val maxMatchDistance =
            Math.max(Math.floor(maxLength / 2.0).toInt() - 1, 0) // look-ahead/-behind to limit transposed matches
        // comparison
        val shorter: String = if (s1.length < s2.length) s1 else s2
        val longer: String = if (s1.length >= s2.length) s1 else s2
        for (i in 0 until shorter.length) {
            // check for exact matches
            var match = shorter[i] == longer[i]
            if (match) {
                if (i < 4) {
                    // prefix match (of at most 4 characters, as described by the algorithm)
                    prefixMatch++
                }
                matches++
                continue
            }
            // check fro transposed matches
            for (j in Math.max(i - maxMatchDistance, 0) until Math.min(i + maxMatchDistance, longer.length)) {
                if (i == j) {
                    // case already covered
                    continue
                }
                // transposition required to match?
                match = shorter[i] == longer[j]
                if (match) {
                    transpositions++
                    break
                }
            }
        }
        // any matching characters?
        if (matches == 0) {
            return 0
        }
        // modify transpositions (according to the algorithm)
        transpositions = (transpositions / 2.0).toInt()
        // non prefix-boosted score
        val score =
            0.3334 * (matches / longer.length.toDouble() + matches / shorter.length.toDouble() + ((matches - transpositions)
                    / matches.toDouble()))
        return if (score < 0.7) {
            score
        } else score + prefixMatch * 0.1 * (1.0 - score)
        // we already have a good match, hence we boost the score proportional to the common prefix
    }

    fun unescapeJavaString(st: String): String {
        val sb = StringBuilder(st.length)
        var i = 0
        while (i < st.length) {
            var ch = st[i]
            if (ch == '\\') {
                val nextChar = if (i == st.length - 1) '\\' else st[i + 1]
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    var code = "" + nextChar
                    i++
                    if (i < st.length - 1 && st[i + 1] >= '0' && st[i + 1] <= '7') {
                        code += st[i + 1]
                        i++
                        if (i < st.length - 1 && st[i + 1] >= '0' && st[i + 1] <= '7') {
                            code += st[i + 1]
                            i++
                        }
                    }
                    sb.append(code.toInt(8).toChar())
                    i++
                    continue
                }
                when (nextChar) {
                    '\\' -> ch = '\\'
                    'b' -> ch = '\b'
                    'f' -> ch = '\f'
                    'n' -> ch = '\n'
                    'r' -> ch = '\r'
                    't' -> ch = '\t'
                    '\"' -> ch = '\"'
                    '\'' -> ch = '\''
                    'u' -> {
                        if (i >= st.length - 5) {
                            ch = 'u'
                            break
                        }
                        val code =
                            ("" + st[i + 2] + st[i + 3]
                                    + st[i + 4] + st[i + 5]).toInt(16)
                        sb.append(Character.toChars(code))
                        i += 5
                        i++
                        continue
                    }
                }
                i++
            }
            sb.append(ch)
            i++
        }
        return sb.toString()
    }
}