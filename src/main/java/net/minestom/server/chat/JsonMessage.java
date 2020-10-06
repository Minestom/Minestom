package net.minestom.server.chat;

import com.google.gson.JsonObject;

/**
 * Represent a json message which can be send to a player
 * <p>
 * Examples are {@link ColoredText} and {@link RichMessage}
 *
 * @see <a href="https://wiki.vg/Chat">Chat Format</a>
 */
public abstract class JsonMessage {

    // true if the compiled string is up-to-date, false otherwise
    private boolean updated;
    // the compiled json string of the message (can be outdated)
    private String compiledJson;

    /**
     * Get the json representation of this message
     * <p>
     * Sent directly to the client
     *
     * @return the json representation of the message
     */
    public abstract JsonObject getJsonObject();

    /**
     * Signal that the final json string changed and that it will need to be updated
     */
    protected void refreshUpdate() {
        this.updated = false;
    }

    /**
     * Get the string json representation
     *
     * @return the string json representation
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
