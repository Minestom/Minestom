package net.minestom.server.chat;

/**
 * Represent a click event for a specific portion of the message
 */
public class ChatClickEvent {

    private String action;
    private String value;

    private ChatClickEvent(String action, String value) {
        this.action = action;
        this.value = value;
    }

    /**
     * Open an URL when clicked
     *
     * @param url the URL to open
     * @return the chat click event
     */
    public static ChatClickEvent openUrl(String url) {
        return new ChatClickEvent("open_url", url);
    }

    /**
     * Run a command when clicked
     *
     * @param command the command to run
     * @return the chat click event
     */
    public static ChatClickEvent runCommand(String command) {
        return new ChatClickEvent("run_command", command);
    }

    /**
     * Write a string in the player's chat when clicked
     *
     * @param command the command to suggest
     * @return the chat click event
     */
    public static ChatClickEvent suggestCommand(String command) {
        return new ChatClickEvent("suggest_command", command);
    }

    protected String getAction() {
        return action;
    }

    protected String getValue() {
        return value;
    }
}
