package net.minestom.server.chat;

public class ChatClickEvent {

    private String action;
    private String value;

    private ChatClickEvent(String action, String value) {
        this.action = action;
        this.value = value;
    }

    public static ChatClickEvent openUrl(String url) {
        return new ChatClickEvent("open_url", url);
    }

    public static ChatClickEvent runCommand(String command) {
        return new ChatClickEvent("run_command", command);
    }

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
