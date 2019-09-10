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

import java.util.ArrayList;
import java.util.List;

public final class TextBuilder {
    private TextObject root;

    // The current text object. This will change when we append text for example
    private TextObject current;

    // The storage of the extra items
    private List<TextObject> extra = new ArrayList<>();

    private TextBuilder(TextObject root) {
        this.root = root;
        this.current = root;
    }

    public static TextBuilder of(String text) {
        return new TextBuilder(new TextObject(text));
    }

    public static TextBuilder empty() {
        return new TextBuilder(null);
    }

    public TextBuilder color(ChatColor color) {
        this.current.setColor(color);
        return this;
    }

    public TextBuilder italicize(boolean b) {
        this.current.setItalic(b);
        return this;
    }

    public TextBuilder bold(boolean b) {
        this.current.setBold(b);
        return this;
    }

    public TextBuilder underline(boolean b) {
        this.current.setUnderlined(b);
        return this;
    }

    public TextBuilder obfuscate(boolean b) {
        this.current.setObfuscated(b);
        return this;
    }

    public TextBuilder strikethrough(boolean b) {
        this.current.setStrikethrough(b);
        return this;
    }

    public TextBuilder clickEvent(ClickEvent clickEvent) {
        this.current.setClickEvent(clickEvent);
        return this;
    }

    public TextBuilder hoverEvent(HoverEvent hoverEvent) {
        this.current.setHoverEvent(hoverEvent);
        return this;
    }

    public TextBuilder append(String text) {
        // essentially this completes what ever object we were on. No turning back!
        return this.appendJson(new TextObject(text));
    }

    public TextBuilder appendJson(TextObject object) {
        if (root == null) {
            this.root = object;
            this.current = object;
        } else {
            this.extra.add(this.current = object);
        }
        return this;
    }

    public TextObject build() {
        // currently we're only adding the extras to the root.
        this.root.setExtra(extra);
        return this.root;
    }

}
