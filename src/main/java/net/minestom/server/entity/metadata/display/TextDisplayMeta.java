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

    public @NotNull Component text() {
        return super.metadata.getIndex(OFFSET, Component.empty());
    }

    public void setText(@NotNull Component value) {
        super.metadata.setIndex(OFFSET, Metadata.Chat(value));
    }

    public int lineWidth() {
        return super.metadata.getIndex(OFFSET + 1, 200);
    }

    public void setLineWidth(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public ARGBColor backgroundColor() {
        return super.metadata.getIndex(OFFSET + 2, new ARGBColor(1073741824));
    }

    public void setBackgroundColor(ARGBColor value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value.asARGB()));
    }

    public int textOpacity() {
        return super.metadata.getIndex(OFFSET + 3, -1);
    }

    public void setTextOpacity(int value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value));
    }

    public boolean shadow() {
        return getMaskBit(OFFSET + 4, SHADOW);
    }

    public void setShadow(boolean value) {
        setMaskBit(OFFSET + 4, SHADOW, value);
    }

    public boolean seeThrough() {
        return getMaskBit(OFFSET + 4, SEE_THROUGH);
    }

    public void setSeeThrough(boolean value) {
        setMaskBit(OFFSET + 4, SEE_THROUGH, value);
    }

    public boolean useDefaultBackground() {
        return getMaskBit(OFFSET + 4, USE_DEFAULT_BACKGROUND);
    }

    public void setUseDefaultBackground(boolean value) {
        setMaskBit(OFFSET + 4, USE_DEFAULT_BACKGROUND, value);
    }

    private boolean alignLeft() {
        return getMaskBit(OFFSET + 4, ALIGN_LEFT);
    }

    private void setAlignLeft(boolean value) {
        setMaskBit(OFFSET + 4, ALIGN_LEFT, value);
    }

    private boolean alignRight() {
        return getMaskBit(OFFSET + 4, ALIGN_RIGHT);
    }

    private void setAlignRight(boolean value) {
        setMaskBit(OFFSET + 4, ALIGN_RIGHT, value);
    }


    public TextAlignment alignment() {
        // TODO get the bit mask directly
        if (alignLeft()) return TextAlignment.LEFT;
        if (alignRight()) return TextAlignment.RIGHT;
        return TextAlignment.CENTER;
    }

    public void setAlignment(TextAlignment value) {
        // TODO set the bit mask directly
        switch (value) {
            case CENTER -> {
                setAlignLeft(false);
                setAlignRight(false);
            }
            case LEFT -> {
                setAlignLeft(true);
                setAlignRight(false);
            }
            case RIGHT -> {
                setAlignLeft(false);
                setAlignRight(true);
            }
        }
    }

    public enum TextAlignment{
        CENTER,
        LEFT,
        RIGHT
    }
}
