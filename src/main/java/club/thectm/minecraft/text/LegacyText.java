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

/**
 * Legacy text is a conversion util which will convert a "legacy" chat string into a TextObject,
 * and a TextObject into a legacy string
 */
public final class LegacyText {

    public static TextObject fromLegacy(String legacyText) {
        return LegacyText.fromLegacy(legacyText, '&');
    }

    /**
     * This function takes in a legacy text string and converts it into a {@link TextObject}.
     * <p>
     * Legacy text strings use the {@link ChatColor#SECTION_SYMBOL}. Many keyboards do not have this symbol however,
     * which is probably why it was chosen. To get around this, it is common practice to substitute
     * the symbol for another, then translate it later. Often '&' is used, but this can differ from person
     * to person. In case the string does not have a {@link ChatColor#SECTION_SYMBOL}, the method also checks for the
     * {@param characterSubstitute}
     *
     * @param legacyText          The text to make into an object
     * @param characterSubstitute The character substitute
     * @return A TextObject representing the legacy text.
     */
    public static TextObject fromLegacy(String legacyText, char characterSubstitute) {
        TextBuilder builder = TextBuilder.of("");
        TextObject currentObject = new TextObject("");
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < legacyText.length(); i++) {
            char c = legacyText.charAt(i);

            if (c == ChatColor.SECTION_SYMBOL || c == characterSubstitute) {
                if ((i + 1) > legacyText.length() - 1) {
                    // do nothing.
                    continue;
                }
                // peek at the next character.
                char peek = legacyText.charAt(i + 1);

                if (ChatColor.isValid(peek)) {
                    i += 1; // if valid
                    if (text.length() > 0) {
                        // create a new text object
                        currentObject.setText(text.toString());

                        // append the current object.
                        builder.appendJson(currentObject);

                        // reset the current object.
                        currentObject = new TextObject("");

                        // reset the buffer
                        text.setLength(0);
                    }

                    ChatColor color = ChatColor.getByCharCode(peek);

                    switch (color) {
                        case OBFUSCATED:
                            currentObject.setObfuscated(true);
                            break;
                        case BOLD:
                            currentObject.setBold(true);
                            break;
                        case STRIKETHROUGH:
                            currentObject.setStrikethrough(true);
                            break;
                        case ITALIC:
                            currentObject.setItalic(true);
                            break;
                        case UNDERLINE:
                            currentObject.setUnderlined(true);
                            break;
                        case RESET:
                            // Reset everything.
                            currentObject.setColor(ChatColor.WHITE);
                            currentObject.setObfuscated(false);
                            currentObject.setBold(false);
                            currentObject.setItalic(false);
                            currentObject.setUnderlined(false);
                            currentObject.setStrikethrough(false);
                            break;
                        default:
                            // emulate Minecraft's behavior of dropping styles that do not yet have an object.
                            currentObject = new TextObject("");
                            currentObject.setColor(color);
                            break;
                    }

                } else {
                    text.append(c);
                }
            } else {
                text.append(c);
            }
        }

        // whatever we were working on when the loop exited
        {
            currentObject.setText(text.toString());
            builder.appendJson(currentObject);
        }

        return builder.build();
    }

    public static String toLegacy(TextObject object) {
        return LegacyText.toLegacy(object, ChatColor.SECTION_SYMBOL);
    }


    /**
     * Takes an {@link TextObject} and transforms it into a legacy string.
     *
     * @param textObject     - The {@link TextObject} to transform to.
     * @param charSubstitute - The substitute character to use if you do not want to use {@link ChatColor#SECTION_SYMBOL}
     * @return A legacy string representation of a text object
     */
    public static String toLegacy(TextObject textObject, char charSubstitute) {
        StringBuilder builder = new StringBuilder();

        if (textObject.getColor() != null) {
            builder.append(charSubstitute).append(textObject.getColor().charCode());
        }


        if (textObject.isObfuscated()) {
            builder.append(charSubstitute).append(ChatColor.OBFUSCATED.charCode());
        }

        if (textObject.isBold()) {
            builder.append(charSubstitute).append(ChatColor.BOLD.charCode());
        }


        if (textObject.isStrikethrough()) {
            builder.append(charSubstitute).append(ChatColor.STRIKETHROUGH.charCode());
        }


        if (textObject.isUnderlined()) {
            builder.append(charSubstitute).append(ChatColor.UNDERLINE.charCode());
        }

        if (textObject.isItalic()) {
            builder.append(charSubstitute).append(ChatColor.ITALIC.charCode());
        }

        if (textObject.getColor() == ChatColor.RESET) {
            builder.setLength(0);
            builder.append(charSubstitute).append(ChatColor.RESET.charCode());
        }

        if (textObject.getText() != null && !textObject.getText().isEmpty()) {
            builder.append(textObject.getText());
        }

        for (TextObject extra : textObject.getExtra()) {
            builder.append(LegacyText.toLegacy(extra, charSubstitute));
        }

        return builder.toString();
    }


}
