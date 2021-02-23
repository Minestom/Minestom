package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CommandBlockMinecartMeta extends AbstractMinecartMeta {

    public CommandBlockMinecartMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public String getCommand() {
        return getMetadata().getIndex((byte) 13, "");
    }

    public void setCommand(@NotNull String value) {
        getMetadata().setIndex((byte) 13, Metadata.String(value));
    }

    @NotNull
    public JsonMessage getLastOutput() {
        return getMetadata().getIndex((byte) 14, ColoredText.of(""));
    }

    public void setLastOutput(@NotNull JsonMessage value) {
        getMetadata().setIndex((byte) 14, Metadata.Chat(value));
    }

}
