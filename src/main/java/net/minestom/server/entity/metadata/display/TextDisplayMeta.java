package net.minestom.server.entity.metadata.display;

import net.kyori.adventure.text.Component;
import net.minestom.server.color.ARGBColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class TextDisplayMeta extends AbstractDisplayMeta {
    public static final byte OFFSET = AbstractDisplayMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 5;

    private static final byte SHADOW = 1;
    private static final byte SEE_THROUGH = 2;
    private static final byte USE_DEFAULT_BACKGROUND = 4;
    private static final byte ALIGN_LEFT = 8;
    private static final byte ALIGN_RIGHT = 16;

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

    public ARGBColor getBackgroundColor() {
        return super.metadata.getIndex(OFFSET + 2, new ARGBColor(1073741824));
    }

    public void setBackgroundColor(ARGBColor value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value.asARGB()));
    }

    public int getTextOpacity() {
        return super.metadata.getIndex(OFFSET + 3, -1);
    }

    public void setTextOpacity(byte value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Byte(value));
    }

    public boolean isShadow() {
        return getMaskBit(OFFSET + 4, SHADOW);
    }

    public void setShadow(boolean value) {
        setMaskBit(OFFSET + 4, SHADOW, value);
    }

    public boolean isSeeThrough() {
        return getMaskBit(OFFSET + 4, SEE_THROUGH);
    }

    public void setSeeThrough(boolean value) {
        setMaskBit(OFFSET + 4, SEE_THROUGH, value);
    }

    public boolean isUseDefaultBackground() {
        return getMaskBit(OFFSET + 4, USE_DEFAULT_BACKGROUND);
    }

    public void setUseDefaultBackground(boolean value) {
        setMaskBit(OFFSET + 4, USE_DEFAULT_BACKGROUND, value);
    }

    public TextAlignment getAlignment() {
        byte mask = getMask(OFFSET + 4);
        boolean leftValue = (mask & ALIGN_LEFT) == ALIGN_LEFT;
        boolean rightValue = (mask & ALIGN_RIGHT) == ALIGN_RIGHT;
        if (leftValue) return TextAlignment.LEFT;
        if (rightValue) return TextAlignment.RIGHT;
        return TextAlignment.CENTER;
    }

    public void setAlignment(TextAlignment value) {
        int index = OFFSET + 4;
        byte mask = getMask(index);
        boolean leftValue = value == TextAlignment.LEFT;
        boolean rightValue = value == TextAlignment.RIGHT;
        boolean currentLeftValue = (mask & ALIGN_LEFT) == ALIGN_LEFT;
        boolean currentRightValue = (mask & ALIGN_RIGHT) == ALIGN_RIGHT;
        if (currentLeftValue == leftValue && currentRightValue == rightValue) return;
        if (currentLeftValue != leftValue) {
            if (leftValue) {
                mask |= ALIGN_LEFT;
            } else {
                mask &= ~ALIGN_LEFT;
            }
        }
        if (currentRightValue != rightValue) {
            if (rightValue) {
                mask |= ALIGN_RIGHT;
            } else {
                mask &= ~ALIGN_RIGHT;
            }
        }
        setMask(index, mask);
    }

    public enum TextAlignment {
        CENTER,
        LEFT,
        RIGHT
    }
}
