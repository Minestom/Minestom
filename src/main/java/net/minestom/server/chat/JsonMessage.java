package net.minestom.server.chat;

import com.google.gson.JsonObject;

/**
 * Represents a json message which can be send to a player.
 * <p>
 * Examples are {@link ColoredText} and {@link RichMessage}.
 *
 * @see <a href="https://wiki.vg/Chat">Chat Format</a>
 */
public abstract class JsonMessage {

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
    public abstract JsonObject getJsonObject();

    /**
     * Signals that the final json string changed and that it will need to be updated.
     */
    protected void refreshUpdate() {
        this.updated = false;
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
    @Override
    public String toString() {
        if (!updated) {
            this.compiledJson = getJsonObject().toString();
            this.updated = true;
        }

        return compiledJson;
    }
}
