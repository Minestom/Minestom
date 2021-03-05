package net.minestom.server.chat;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a click event for a specific portion of the message.
 */
public class ChatClickEvent {

    private final String action;
    private final String value;

    private ChatClickEvent(@NotNull String action, @NotNull String value) {
        this.action = action;
        this.value = value;
    }

    /**
     * Opens an URL when clicked.
     *
     * @param url the URL to open
     * @return the chat click event
     */
    @NotNull
    public static ChatClickEvent openUrl(@NotNull String url) {
        return new ChatClickEvent("open_url", url);
    }

    /**
     * Runs a command when clicked.
     *
     * @param command the command to run
     * @return the chat click event
     */
    @NotNull
    public static ChatClickEvent runCommand(@NotNull String command) {
        return new ChatClickEvent("run_command", command);
    }

    /**
     * Writes a string in the player's chat when clicked.
     *
     * @param command the command to suggest
     * @return the chat click event
     */
    @NotNull
    public static ChatClickEvent suggestCommand(@NotNull String command) {
        return new ChatClickEvent("suggest_command", command);
    }

    @NotNull
    public static ChatClickEvent copyToClipboard(@NotNull String text) {
        return new ChatClickEvent("copy_to_clipboard", text);
    }

    @NotNull
    protected String getAction() {
        return action;
    }

    @NotNull
    protected String getValue() {
        return value;
    }
}
