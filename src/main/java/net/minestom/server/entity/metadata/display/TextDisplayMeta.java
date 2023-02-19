package net.minestom.server.entity.metadata.display;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class TextDisplayMeta extends AbstractDisplayMeta {
    public static final byte OFFSET = AbstractDisplayMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 5;

    public TextDisplayMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public @NotNull Component getText() {
        return super.metadata.getIndex(OFFSET, Component.empty());
    }

    public void setText(@NotNull Component value) {
        super.metadata.setIndex(OFFSET, Metadata.Chat(value));
    }

    public int getLineWidth() {
        return super.metadata.getIndex(OFFSET + 1, 200);
    }

    public void setLineWidth(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public int getBackgroundColor() {
        return super.metadata.getIndex(OFFSET + 2, 1073741824);
    }

    public void setBackgroundColor(int value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value));
    }

    public int getTextOpacity() {
        return super.metadata.getIndex(OFFSET + 3, -1);
    }

    public void setTextOpacity(int value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value));
    }

    public byte getStyleFlags() {
        return super.metadata.getIndex(OFFSET + 4, (byte)0);
    }

    public void setStyleFlags(byte value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Byte(value));
    }

}
