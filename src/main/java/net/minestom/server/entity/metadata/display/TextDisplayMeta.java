package net.minestom.server.entity.metadata.display;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class TextDisplayMeta extends AbstractDisplayMeta {
    public TextDisplayMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Component getText() {
        return get(MetadataDef.TextDisplay.TEXT);
    }

    public void setText(Component value) {
        set(MetadataDef.TextDisplay.TEXT, value);
    }

    public int getLineWidth() {
        return get(MetadataDef.TextDisplay.LINE_WIDTH);
    }

    public void setLineWidth(int value) {
        set(MetadataDef.TextDisplay.LINE_WIDTH, value);
    }

    public int getBackgroundColor() {
        return get(MetadataDef.TextDisplay.BACKGROUND_COLOR);
    }

    public void setBackgroundColor(int value) {
        set(MetadataDef.TextDisplay.BACKGROUND_COLOR, value);
    }

    public byte getTextOpacity() {
        return get(MetadataDef.TextDisplay.TEXT_OPACITY);
    }

    public void setTextOpacity(byte value) {
        set(MetadataDef.TextDisplay.TEXT_OPACITY, value);
    }

    public boolean isShadow() {
        return get(MetadataDef.TextDisplay.HAS_SHADOW);
    }

    public void setShadow(boolean value) {
        set(MetadataDef.TextDisplay.HAS_SHADOW, value);
    }

    public boolean isSeeThrough() {
        return get(MetadataDef.TextDisplay.IS_SEE_THROUGH);
    }

    public void setSeeThrough(boolean value) {
        set(MetadataDef.TextDisplay.IS_SEE_THROUGH, value);
    }

    public boolean isUseDefaultBackground() {
        return get(MetadataDef.TextDisplay.USE_DEFAULT_BACKGROUND_COLOR);
    }

    public void setUseDefaultBackground(boolean value) {
        set(MetadataDef.TextDisplay.USE_DEFAULT_BACKGROUND_COLOR, value);
    }

    public boolean isAlignLeft() {
        return get(MetadataDef.TextDisplay.ALIGN_LEFT);
    }

    public void setAlignLeft(boolean value) {
        set(MetadataDef.TextDisplay.ALIGN_LEFT, value);
    }

    public boolean isAlignRight() {
        return get(MetadataDef.TextDisplay.ALIGN_RIGHT);
    }

    public void setAlignRight(boolean value) {
        set(MetadataDef.TextDisplay.ALIGN_RIGHT, value);
    }

    public Alignment getAlignment() {
        return Alignment.fromId(get(MetadataDef.TextDisplay.ALIGNMENT));
    }

    public void setAlignment(Alignment value) {
        set(MetadataDef.TextDisplay.ALIGNMENT, (byte) value.ordinal());
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
