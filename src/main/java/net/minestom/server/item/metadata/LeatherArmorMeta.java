package net.minestom.server.item.metadata;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represents the item meta for leather armor parts.
 */
public class LeatherArmorMeta extends ItemMeta {
    private boolean modified;
    private Color color;

    /**
     * Sets the color of the leather armor piece.
     *
     * @param color the color of the leather armor
     * @deprecated Use {@link #setColor(Color)}
     */
    @Deprecated
    public void setColor(ChatColor color) {
        this.setColor(color.asColor());
    }

    /**
     * Changes the color of the leather armor piece.
     *
     * @param red   The red color of the leather armor piece.
     * @param green The green color of the leather armor piece.
     * @param blue  The blue color of the leather armor piece.
     * @deprecated Use {@link #setColor(Color)}
     */
    @Deprecated
    public void setColor(byte red, byte green, byte blue) {
        this.setColor(new Color(red, green, blue));
    }

    /**
     * Sets the color of this leather armor piece.
     *
     * @param color the new color
     */
    public void setColor(@NotNull Color color) {
        this.modified = !color.equals(this.color);
        this.color = color;
    }

    /**
     * Gets the color of this leather armor piece.
     *
     * @return the color
     */
    public @NotNull Color getColor() {
        return this.color;
    }

    /**
     * Resets the color to the default leather one.
     */
    public void reset() {
        this.color = new Color(0, 0, 0);
        this.modified = false;
    }

    /**
     * Gets the red component.
     *
     * @return the red component
     * @deprecated Use {@link #getColor}
     */
    @Deprecated
    public int getRed() {
        return this.color.getRed();
    }

    /**
     * Gets the green component.
     *
     * @return the green component
     * @deprecated Use {@link #getColor}
     */
    @Deprecated
    public int getGreen() {
        return this.color.getGreen();
    }

    /**
     * Gets the blue component.
     *
     * @return the blue component
     * @deprecated Use {@link #getColor}
     */
    @Deprecated
    public int getBlue() {
        return this.color.getBlue();
    }

    /**
     * Gets if the color of this armor piece have been changed.
     *
     * @return true if the color has been changed, false otherwise
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNbt() {
        return modified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof LeatherArmorMeta)) return false;
        final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
        return leatherArmorMeta.isModified() == isModified()
                && leatherArmorMeta.getColor().equals(getColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("display")) {
            final NBTCompound nbtCompound = compound.getCompound("display");
            if (nbtCompound.containsKey("color")) {
                final int color = nbtCompound.getInt("color");

                // Sets the color of the leather armor piece
                // This also fixes that the armor pieces do not decolorize again when you are in creative
                // mode.
                this.setColor(new Color(color));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull NBTCompound compound) {
        if (modified) {
            NBTCompound displayCompound;
            if (!compound.containsKey("display")) {
                displayCompound = new NBTCompound();
            } else {
                displayCompound = compound.getCompound("display");
            }
            displayCompound.setInt("color", color.asRGB());
            // Adds the color compound to the display compound
            compound.set("display", displayCompound);
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ItemMeta clone() {
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) super.clone();
        leatherArmorMeta.modified = this.isModified();
        leatherArmorMeta.color = color;

        return leatherArmorMeta;
    }
}
