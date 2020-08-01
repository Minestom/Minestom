package net.minestom.server.item.metadata;

import net.minestom.server.chat.ChatColor;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class LeatherArmorMeta implements ItemMeta {

    private boolean modified;
    private int r, g, b;

    /**
     * Set the color of the leather armor piece
     *
     * @param color the color of the leather armor
     */
    public void setColor(ChatColor color) {
        // TODO using "CHAT color" is pretty weird, maybe that the class should be renamed to "Color"
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.modified = true;
    }

    /**
     * Reset the color to the default leather one
     */
    public void reset() {
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.modified = false;
    }

    /**
     * Get the red component
     *
     * @return the red component
     */
    public int getRed() {
        return r;
    }

    /**
     * Get the green component
     *
     * @return the green component
     */
    public int getGreen() {
        return g;
    }

    /**
     * Get the blue component
     *
     * @return the blue component
     */
    public int getBlue() {
        return b;
    }

    /**
     * Get if the color of this armor piece have been changed
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
    public boolean isSimilar(ItemMeta itemMeta) {
        if (!(itemMeta instanceof LeatherArmorMeta))
            return false;
        final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
        return leatherArmorMeta.modified == modified &&
                leatherArmorMeta.r == r &&
                leatherArmorMeta.g == g &&
                leatherArmorMeta.b == b;
    }

    @Override
    public void read(NBTCompound compound) {
        if (compound.containsKey("display")) {
            final NBTCompound nbtCompound = compound.getCompound("display");
            if (nbtCompound.containsKey("color")) {
                final int color = nbtCompound.getInt("color");
                this.r = (color >> 16) & 0x000000FF;
                this.g = (color >> 8) & 0x000000FF;
                this.b = (color) & 0x000000FF;
            }
        }
    }

    @Override
    public void write(NBTCompound compound) {
        if (modified) {
            NBTCompound displayCompound;
            if (!compound.containsKey("display")) {
                displayCompound = new NBTCompound();
            } else {
                displayCompound = compound.getCompound("display");
            }
            final int color = r << 16 + g << 8 + b;
            displayCompound.setInt("color", color);
        }
    }

    @Override
    public ItemMeta clone() {
        LeatherArmorMeta leatherArmorMeta = new LeatherArmorMeta();
        leatherArmorMeta.modified = modified;
        leatherArmorMeta.r = r;
        leatherArmorMeta.g = g;
        leatherArmorMeta.b = b;

        return leatherArmorMeta;
    }
}
