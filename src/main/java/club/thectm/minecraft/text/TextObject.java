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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@ToString
public final class TextObject {

    private String text;
    private ChatColor color;
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;
    private boolean italic, bold, underlined, obfuscated, strikethrough;
    private List<TextObject> extra = new ArrayList<>();

    public TextObject(String text) {
        this.text = text;
    }

    public static TextObject fromJson(JsonObject object) {
        if (object.has("text")) {
            TextObject o = new TextObject(object.get("text").getAsString());

            if (object.has("clickEvent")) {
                o.setClickEvent(ClickEvent.fromJson(object.get("clickEvent").getAsJsonObject()));
            }

            if (object.has("hoverEvent")) {
                o.setHoverEvent(HoverEvent.fromJson(object.get("hoverEvent").getAsJsonObject()));
            }

            if (object.has("color")) {
                o.setColor(ChatColor.valueOf(object.get("color").getAsString().toUpperCase()));
            }

            if (object.has("obfuscated"))
                o.setObfuscated(object.get("obfuscated").getAsBoolean());
            if (object.has("italic"))
                o.setItalic(object.get("italic").getAsBoolean());
            if (object.has("bold"))
                o.setBold(object.get("bold").getAsBoolean());
            if (object.has("underlined"))
                o.setUnderlined(object.get("underlined").getAsBoolean());

            if (object.has("extra")) {
                for (JsonElement extra : object.getAsJsonArray("extra")) {
                    if (extra.isJsonObject()) {
                        TextObject e = TextObject.fromJson(extra.getAsJsonObject());
                        if (e != null) {
                            o.extra.add(e);
                        }
                    }
                }
            }

            return o;
        }

        // invalid object
        return null;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public void setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    public void setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public void setExtra(List<TextObject> extra) {
        this.extra.addAll(extra);
    }

    public void setText(String text) {
        this.text = text;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("text", text);

        if (color != null)
            object.addProperty("color", color.toJsonString());
        if (clickEvent != null)
            object.add("clickEvent", clickEvent.toJson());
        if (hoverEvent != null)
            object.add("hoverEvent", hoverEvent.toJson());
        if (italic)
            object.addProperty("italic", true);
        if (bold)
            object.addProperty("bold", true);
        if (underlined)
            object.addProperty("underlined", true);
        if (obfuscated)
            object.addProperty("obfuscated", true);
        if (strikethrough)
            object.addProperty("strikethrough", true);

        if (!extra.isEmpty()) {
            JsonArray array = new JsonArray();

            for (TextObject ex : extra) {
                array.add(ex.toJson());
            }

            object.add("extra", array);
        }
        return object;
    }
}