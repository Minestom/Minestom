package fr.themode.minestom.event;

import club.thectm.minecraft.text.TextObject;
import fr.themode.minestom.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class PlayerChatEvent extends CancellableEvent {

    private Player sender;
    private Collection<Player> recipients;
    private String message;
    private Function<PlayerChatEvent, TextObject> chatFormat;

    public PlayerChatEvent(Player sender, Collection<Player> recipients, String message) {
        this.sender = sender;
        this.recipients = new ArrayList<>(recipients);
        this.message = message;
    }

    public void setChatFormat(Function<PlayerChatEvent, TextObject> chatFormat) {
        this.chatFormat = chatFormat;
    }

    public Player getSender() {
        return sender;
    }

    public Collection<Player> getRecipients() {
        return recipients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Function<PlayerChatEvent, TextObject> getChatFormatFunction() {
        return chatFormat;
    }
}
