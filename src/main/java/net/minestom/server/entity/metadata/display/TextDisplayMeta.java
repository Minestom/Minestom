package net.minestom.server.entity.metadata.display;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class TextDisplayMeta extends AbstractDisplayMeta {
    public TextDisplayMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull Component getText() {
        return metadata.get(MetadataDef.TextDisplay.TEXT);
    }

    public void setText(@NotNull Component value) {
        metadata.set(MetadataDef.TextDisplay.TEXT, value);
    }

    public int getLineWidth() {
        return metadata.get(MetadataDef.TextDisplay.LINE_WIDTH);
    }

    public void setLineWidth(int value) {
        metadata.set(MetadataDef.TextDisplay.LINE_WIDTH, value);
    }

    public int getBackgroundColor() {
        return metadata.get(MetadataDef.TextDisplay.BACKGROUND_COLOR);
    }

    public void setBackgroundColor(int value) {
        metadata.set(MetadataDef.TextDisplay.BACKGROUND_COLOR, value);
    }

    public byte getTextOpacity() {
        return metadata.get(MetadataDef.TextDisplay.TEXT_OPACITY);
    }

    public void setTextOpacity(byte value) {
        metadata.set(MetadataDef.TextDisplay.TEXT_OPACITY, value);
    }

    public boolean isShadow() {
        return metadata.get(MetadataDef.TextDisplay.HAS_SHADOW);
    }

    public void setShadow(boolean value) {
        metadata.set(MetadataDef.TextDisplay.HAS_SHADOW, value);
    }

    public boolean isSeeThrough() {
        return metadata.get(MetadataDef.TextDisplay.IS_SEE_THROUGH);
    }

    public void setSeeThrough(boolean value) {
        metadata.set(MetadataDef.TextDisplay.IS_SEE_THROUGH, value);
    }

    public boolean isUseDefaultBackground() {
        return metadata.get(MetadataDef.TextDisplay.USE_DEFAULT_BACKGROUND_COLOR);
    }

    public void setUseDefaultBackground(boolean value) {
        metadata.set(MetadataDef.TextDisplay.USE_DEFAULT_BACKGROUND_COLOR, value);
    }

    public boolean isAlignLeft() {
        return metadata.get(MetadataDef.TextDisplay.ALIGN_LEFT);
    }

    public void setAlignLeft(boolean value) {
        metadata.set(MetadataDef.TextDisplay.ALIGN_LEFT, value);
    }

    public boolean isAlignRight() {
        return metadata.get(MetadataDef.TextDisplay.ALIGN_RIGHT);
    }

    public void setAlignRight(boolean value) {
        metadata.set(MetadataDef.TextDisplay.ALIGN_RIGHT, value);
    }

    public Alignment getAlignment() {
        return Alignment.fromId(metadata.get(MetadataDef.TextDisplay.ALIGNMENT));
    }

    public void setAlignment(Alignment value) {
        metadata.set(MetadataDef.TextDisplay.ALIGNMENT, (byte) value.ordinal());
    }

    public enum Alignment {
        CENTER,
        LEFT,
        RIGHT;

        private final static Alignment[] VALUES = values();

        private static Alignment fromId(int id) {
            if (id >= 0 && id < VALUES.length) {
                return VALUES[id];
            }
            return CENTER;
        }
    }

}
