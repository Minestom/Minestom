package net.minestom.server.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a json message which can be send to a player.
 * <p>
 * Examples are {@link ColoredText} and {@link RichMessage}.
 *
 * @see <a href="https://wiki.vg/Chat">Chat Format</a>
 * @deprecated Use {@link Component}
 */
@Deprecated
public abstract class JsonMessage implements ComponentLike {

    // true if the compiled string is up-to-date, false otherwise
    private boolean updated;
    // the compiled json string of the message (can be outdated)
    private String compiledJson;

    /**
     * Gets the json representation of this message.
     * <p>
     * Sent directly to the client.
     *
     * @return the json representation of the message
     * @see #toString()
     */
    @NotNull
    public abstract JsonObject getJsonObject();

    /**
     * Signals that the final json string changed and that it will need to be updated.
     */
    protected void refreshUpdate() {
        this.updated = false;
    }

    /**
     * Gets the content of the message without any formatting or effects.
     *
     * @return The message without formatting or effects
     */
    @NotNull
    public String getRawMessage() {
        return getTextMessage(getJsonObject()).toString();
    }

    @Override
    public @NotNull Component asComponent() {
        return GsonComponentSerializer.gson().deserializeFromTree(this.getJsonObject());
    }

    public static @NotNull JsonMessage fromComponent(@NotNull Component component) {
        return new RawJsonMessage(GsonComponentSerializer.gson().serializer().toJsonTree(component).getAsJsonObject());
    }

    /**
     * Gets the Json representation.
     * <p>
     * Will check of the current cached compiled json is up-to-date in order to prevent
     * re-parsing the message every time.
     *
     * @return the string json representation
     * @see #getJsonObject()
     * @see #refreshUpdate()
     */
    @NotNull
    @Override
    public String toString() {
        if (!updated) {
            this.compiledJson = getJsonObject().toString();
            this.updated = true;
        }

        return compiledJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonMessage message = (JsonMessage) o;
        return Objects.equals(toString(), message.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Recursively collects the 'text' field from the provided object and it's 'extra's.
     *
     * @param obj The object to parse
     * @return The text content of the object and its 'extra's
     */
    @NotNull
    private static StringBuilder getTextMessage(@NotNull JsonObject obj) {
        StringBuilder message = new StringBuilder(obj.get("text").getAsString());
        JsonElement extra = obj.get("extra");
        if (extra != null && extra.isJsonArray()) {
            for (JsonElement child : extra.getAsJsonArray()) {
                if (!child.isJsonObject()) continue;
                message.append(getTextMessage(child.getAsJsonObject()));
            }
        }
        return message;
    }

    @Deprecated
    public static class RawJsonMessage extends JsonMessage {

        private final JsonObject jsonObject;

        public RawJsonMessage(@NotNull JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public RawJsonMessage(@NotNull String jsonObject) {
            this.jsonObject = JsonParser.parseString(jsonObject).getAsJsonObject();
        }

        @NotNull
        @Override
        public JsonObject getJsonObject() {
            return jsonObject;
        }
    }

}
