package net.minestom.server.entity.metadata.minecart;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CommandBlockMinecartMeta extends AbstractMinecartMeta {
    public static final byte OFFSET = AbstractMinecartMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public CommandBlockMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public String getCommand() {
        return super.metadata.getIndex(OFFSET, "");
    }

    public void setCommand(@NotNull String value) {
        super.metadata.setIndex(OFFSET, Metadata.String(value));
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
        return super.metadata.getIndex(OFFSET + 1, Component.empty());
    }

    /**
     * @deprecated Use {@link #setLastOutput(Component)}
     */
    @Deprecated
    public void setLastOutput(@NotNull JsonMessage value) {
        this.setLastOutput(value.asComponent());
    }

    public void setLastOutput(@NotNull Component value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Chat(value));
    }

    @Override
    public int getObjectData() {
        return 6;
    }

}
