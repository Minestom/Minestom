package net.minestom.server.item.firework;

import net.minestom.server.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FireworkEffect {

    private final boolean flicker;
    private final boolean trail;
    private final FireworkEffectType type;
    private final List<Color> color;
    private final List<Color> fadeColor;

    //FIXME: fix javadoc

    /**
     * Initializes a new firework effect.
     *
     * @param flicker   {@code true} if this explosion has the Twinkle effect (glowstone dust), otherwise {@code false}.
     * @param trail     {@code true} if this explosion hsa the Trail effect (diamond), otherwise {@code false}.
     * @param type      The shape of this firework's explosion.
     * @param color     The primary colors of this firework effect.
     * @param fadeColor The secondary colors of this firework effect.
     */
    public FireworkEffect(boolean flicker, boolean trail, FireworkEffectType type, List<Color> color, List<Color> fadeColor) {
        this.flicker = flicker;
        this.trail = trail;
        this.type = type;
        this.color = color;
        this.fadeColor = fadeColor;
    }

    /**
     * Retrieves a firework effect from the given {@code compound}.
     *
     * @param compound The NBT connection, which should be a fireworks effect.
     * @return A new created firework effect.
     */
    public static FireworkEffect fromCompound(@NotNull NBTCompound compound) {

        List<Color> primaryColor = new ArrayList<>();
        List<Color> secondaryColor = new ArrayList<>();

        if (compound.containsKey("Colors")) {
            final int[] color = compound.getIntArray("Colors");
            for (int j : color) {
                primaryColor.add(new Color(j));
            }
        }

        if (compound.containsKey("FadeColors")) {
            final int[] fadeColor = compound.getIntArray("FadeColors");
            for (int j : fadeColor) {
                secondaryColor.add(new Color(j));
            }
        }

        boolean flicker = compound.containsKey("Flicker") && compound.getAsByte("Flicker") == 1;
        boolean trail = compound.containsKey("Trail") && compound.getAsByte("Trail") == 1;
        FireworkEffectType type = compound.containsKey("Type") ? FireworkEffectType.byId(compound.getAsByte("Type")) : FireworkEffectType.SMALL_BALL;


        return new FireworkEffect(
                flicker,
                trail,
                type,
                primaryColor,
                secondaryColor);
    }

    /**
     * Whether the firework has a flicker effect.
     *
     * @return {@code 1} if this explosion has the flicker effect, otherwise {@code 0}.
     */
    public byte getFlicker() {
        return (byte) (this.flicker ? 1 : 0);
    }

    /**
     * Whether the firework has a trail effect.
     *
     * @return {@code 1} if this explosion has the trail effect, otherwise {@code 0};
     */
    public byte getTrail() {
        return (byte) (this.trail ? 1 : 0);
    }

    /**
     * Retrieves type of the firework effect.
     *
     * @return The firework type as a byte.
     */
    public byte getType() {
        return this.type.getType();
    }

    /**
     * Retrieves array of integer values corresponding to the primary colors of this firework's explosion.
     * <p>
     * If custom color codes are used, the game renders is as "Custom" in the tooltip, but the proper color is used
     * in the explosion.
     *
     * @return An array of integer values corresponding to the primary colors of this firework's explosion.
     */
    public int[] getColors() {
        int[] primary = new int[color.size()];
        for (int i = 0; i < color.size(); i++) {
            primary[i] = color.get(i).asRGB();
        }
        return primary;
    }

    /**
     * Retrieves array of integer values corresponding to the fading colors of this firework's explosion.
     * <p>
     * Same handling of custom colors as Colors.
     *
     * @return An array of integer values corresponding to the fading colors of this firework's explosion.
     */
    public int[] getFadeColors() {
        int[] secondary = new int[fadeColor.size()];
        for (int i = 0; i < fadeColor.size(); i++) {
            secondary[i] = fadeColor.get(i).asRGB();
        }
        return secondary;
    }

    /**
     * Retrieves the {@link FireworkEffect} as a {@link NBTCompound}.
     *
     * @return The firework effect as a nbt compound.
     */
    public NBTCompound asCompound() {
        NBTCompound explosionCompound = new NBTCompound();
        explosionCompound.setByte("Flicker", this.getFlicker());
        explosionCompound.setByte("Trail", this.getTrail());
        explosionCompound.setByte("Type", this.getType());

        explosionCompound.setIntArray("Colors", this.getColors());
        explosionCompound.setIntArray("FadeColors", this.getFadeColors());

        return explosionCompound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FireworkEffect that = (FireworkEffect) o;
        return flicker == that.flicker &&
                trail == that.trail &&
                type == that.type &&
                Objects.equals(color, that.color) &&
                Objects.equals(fadeColor, that.fadeColor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(flicker, trail, type, color, fadeColor);
    }
}
