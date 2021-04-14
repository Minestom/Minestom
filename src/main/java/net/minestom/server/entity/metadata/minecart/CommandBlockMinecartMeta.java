package net.minestom.server.entity.metadata.minecart;

import net.kyori.adventure.text.Component;
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

    /**
     * @deprecated Use {@link #getLastOutput()}
     */
    @Deprecated
    @NotNull
    public JsonMessage getLastOutputJson() {
        return JsonMessage.fromComponent(getLastOutput());
    }

    @NotNull
    public Component getLastOutput() {
        return super.metadata.getIndex((byte) 14, Component.empty());
    }

    /**
     * @deprecated Use {@link #setLastOutput(Component)}
     */
    @Deprecated
    public void setLastOutput(@NotNull JsonMessage value) {
        this.setLastOutput(value.asComponent());
    }

    public void setLastOutput(@NotNull Component value) {
        super.metadata.setIndex((byte) 14, Metadata.Chat(value));
    }

    @Override
    public int getObjectData() {
        return 6;
    }

}
