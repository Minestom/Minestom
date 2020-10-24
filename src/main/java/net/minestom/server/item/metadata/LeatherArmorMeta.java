package net.minestom.server.item.metadata;

import net.minestom.server.chat.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class LeatherArmorMeta implements ItemMeta {

    private boolean modified;
    private byte red, green, blue;

    /**
     * Sets the color of the leather armor piece.
     *
     * @param color the color of the leather armor
     */
    public void setColor(ChatColor color) {
        // TODO using "CHAT color" is pretty weird, maybe that the class should be renamed to "Color"
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.modified = true;
    }

    /**
     * Resets the color to the default leather one.
     */
    public void reset() {
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.modified = false;
    }

    /**
     * Gets the red component.
     *
     * @return the red component
     */
    public int getRed() {
        return red;
    }

    /**
     * Gets the green component.
     *
     * @return the green component
     */
    public int getGreen() {
        return green;
    }

    /**
     * Gets the blue component.
     *
     * @return the blue component
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Gets if the color of this armor piece have been changed.
     *
     * @return true if the color has been changed, false otherwise
     */
    public boolean isModified() {
        return modified;
    }

    @Override
    public boolean hasNbt() {
        return modified;
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof LeatherArmorMeta))
            return false;
        final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
        return leatherArmorMeta.modified == modified &&
                leatherArmorMeta.red == red &&
                leatherArmorMeta.green == green &&
                leatherArmorMeta.blue == blue;
    }

    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("display")) {
            final NBTCompound nbtCompound = compound.getCompound("display");
            if (nbtCompound.containsKey("color")) {
                final int color = nbtCompound.getInt("color");
                this.red = (byte) ((color >> 16) & 0x000000FF);
                this.green = (byte) ((color >> 8) & 0x000000FF);
                this.blue = (byte) ((color) & 0x000000FF);
            }
        }
    }

    @Override
    public void write(@NotNull NBTCompound compound) {
        if (modified) {
            NBTCompound displayCompound;
            if (!compound.containsKey("display")) {
                displayCompound = new NBTCompound();
            } else {
                displayCompound = compound.getCompound("display");
            }
            final int color = red << 16 + green << 8 + blue;
            displayCompound.setInt("color", color);
        }
    }

    @NotNull
    @Override
    public ItemMeta clone() {
        LeatherArmorMeta leatherArmorMeta = new LeatherArmorMeta();
        leatherArmorMeta.modified = modified;
        leatherArmorMeta.red = red;
        leatherArmorMeta.green = green;
        leatherArmorMeta.blue = blue;

        return leatherArmorMeta;
    }
}
