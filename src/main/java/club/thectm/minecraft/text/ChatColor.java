/*
 * MIT License
 *
 * Copyright (c) 2018 Ryan Willette
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package club.thectm.minecraft.text;

public enum ChatColor {
    // Colors
    BLACK,
    DARK_BLUE,
    DARK_GREEN,
    DARK_AQUA,
    DARK_RED,
    DARK_PURPLE,
    GOLD,
    GRAY,
    DARK_GRAY,
    BLUE,
    GREEN,
    AQUA,
    RED,
    LIGHT_PURPLE,
    YELLOW,
    WHITE,
    // Styles
    OBFUSCATED(true),
    BOLD(true),
    STRIKETHROUGH(true),
    UNDERLINE(true),
    ITALIC(true),
    RESET(true);


    public static final char SECTION_SYMBOL = '\u00A7';

    private static final String CHARS_STRING = "0123456789abcdefklmnor";
    private static final char[] CHARS = CHARS_STRING.toCharArray();

    private boolean format;


    ChatColor(boolean format) {
        this.format = format;
    }

    ChatColor() {
        this(false);
    }

    public static boolean isValid(char c) {
        return CHARS_STRING.indexOf(Character.toLowerCase(c)) != -1;
    }

    public static ChatColor getByCharCode(char c) {
        return ChatColor.values()[CHARS_STRING.indexOf(Character.toLowerCase(c))];
    }


    public boolean isFormat() {
        return format;
    }

    public char charCode() {
        return CHARS[this.ordinal()];
    }


    public String toLegacyString() {
        return String.valueOf(new char[]{SECTION_SYMBOL, this.charCode()});
    }

    public String toJsonString() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.toLegacyString();
    }
}
