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

import com.google.gson.JsonObject;

public final class HoverEvent {
    private Action action;
    private String value;

    public HoverEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public static HoverEvent fromJson(JsonObject object) {
        String action = object.getAsJsonPrimitive("action").getAsString();
        String value = object.getAsJsonPrimitive("value").getAsString();

        return new HoverEvent(Action.valueOf(action), value);
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("action", action.toString());
        object.addProperty("value", value);

        return object;
    }


    public enum Action {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
