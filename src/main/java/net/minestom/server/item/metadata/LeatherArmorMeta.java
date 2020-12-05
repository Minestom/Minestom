package net.minestom.server.item.metadata;

import net.minestom.server.chat.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/** Represents the item meat for leather armor parts. */
public class LeatherArmorMeta implements ItemMeta {

  private static final int BIT_MASK = 0xFF;

  private boolean modified;
  private byte red;
  private byte green;
  private byte blue;

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
   * Changes the color of the leather armor piece.
   *
   * @param red The red color of the leather armor piece.
   * @param green The green color of the leather armor piece.
   * @param blue The blue color of the leather armor piece.
   */
  public void setColor(byte red, byte green, byte blue) {
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.modified = true;
  }

  /** Resets the color to the default leather one. */
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
    return BIT_MASK & red;
  }

  /**
   * Gets the green component.
   *
   * @return the green component
   */
  public int getGreen() {
    return BIT_MASK & green;
  }

  /**
   * Gets the blue component.
   *
   * @return the blue component
   */
  public int getBlue() {
    return BIT_MASK & blue;
  }

  /**
   * Gets if the color of this armor piece have been changed.
   *
   * @return true if the color has been changed, false otherwise
   */
  public boolean isModified() {
    return modified;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasNbt() {
    return modified;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSimilar(@NotNull ItemMeta itemMeta) {
    if (!(itemMeta instanceof LeatherArmorMeta)) return false;
    final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
    return leatherArmorMeta.isModified() == isModified()
        && leatherArmorMeta.getRed() == getRed()
        && leatherArmorMeta.getGreen() == getGreen()
        && leatherArmorMeta.getBlue() == getBlue();
  }

  /** {@inheritDoc} */
  @Override
  public void read(@NotNull NBTCompound compound) {
    if (compound.containsKey("display")) {
      final NBTCompound nbtCompound = compound.getCompound("display");
      if (nbtCompound.containsKey("color")) {
        final int color = nbtCompound.getInt("color");

        // Sets the color of the leather armor piece
        // This also fixes that the armor pieces do not decolorize again when you are in creative
        // mode.
        this.setColor(
            (byte) ((color >> 16) & BIT_MASK),
            (byte) ((color >> 8) & BIT_MASK),
            (byte) ((color) & BIT_MASK));
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void write(@NotNull NBTCompound compound) {
    if (modified) {
      NBTCompound displayCompound;
      if (!compound.containsKey("display")) {
        displayCompound = new NBTCompound();
      } else {
        displayCompound = compound.getCompound("display");
      }
      final int color = this.getRed() << 16 | this.getGreen() << 8 | this.getBlue();

      displayCompound.setInt("color", color);
      // Adds the color compound to the display compound
      compound.set("display", displayCompound);
    }
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public ItemMeta copy() {
    LeatherArmorMeta leatherArmorMeta = new LeatherArmorMeta();
    leatherArmorMeta.modified = this.isModified();
    leatherArmorMeta.red = (byte) this.getRed();
    leatherArmorMeta.green = (byte) this.getGreen();
    leatherArmorMeta.blue = (byte) this.getBlue();

    return leatherArmorMeta;
  }
}
