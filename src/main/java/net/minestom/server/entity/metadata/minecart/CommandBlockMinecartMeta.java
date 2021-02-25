package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CommandBlockMinecartMeta extends AbstractMinecartMeta {

    public CommandBlockMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public String getCommand() {
        return super.metadata.getIndex((byte) 13, "");
    }

    public void setCommand(@NotNull String value) {
        super.metadata.setIndex((byte) 13, Metadata.String(value));
    }

    @NotNull
    public JsonMessage getLastOutput() {
        return super.metadata.getIndex((byte) 14, ColoredText.of(""));
    }

    public void setLastOutput(@NotNull JsonMessage value) {
        super.metadata.setIndex((byte) 14, Metadata.Chat(value));
    }

    @Override
    public int getObjectData() {
        return 6;
    }

}
