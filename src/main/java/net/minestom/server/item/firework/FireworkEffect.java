package net.minestom.server.item.firework;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.item.metadata.FireworkMeta;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class FireworkEffect {

    private final boolean flicker;
    private final boolean trail;
    private final FireworkEffectType type;
    private final ChatColor color;
    private final ChatColor fadeColor;

    /**
     * Initializes a new firework effect.
     *
     * @param flicker   {@code true} if this explosion has the Twinkle effect (glowstone dust), otherwise {@code false}.
     * @param trail     {@code true} if this explosion hsa the Trail effect (diamond), otherwise {@code false}.
     * @param type      The shape of this firework's explosion.
     * @param color
     * @param fadeColor
     */
    public FireworkEffect(boolean flicker, boolean trail, FireworkEffectType type, ChatColor color, ChatColor fadeColor) {
        this.flicker = flicker;
        this.trail = trail;
        this.type = type;
        this.color = color;
        this.fadeColor = fadeColor;
    }

    public static FireworkEffect fromCompound(NBTCompound compound) {

        ChatColor primaryColor = null;
        ChatColor secondaryColor = null;

        if (compound.containsKey("Colors")) {
            int[] color = compound.getIntArray("Colors");
            primaryColor = ChatColor.fromRGB((byte) color[0], (byte) color[1], (byte) color[2]);
        }

        if (compound.containsKey("FadeColors")) {
            int[] fadeColor = compound.getIntArray("FadeColors");
            secondaryColor = ChatColor.fromRGB((byte) fadeColor[0], (byte) fadeColor[1], (byte) fadeColor[2]);

        }

        boolean flicker = compound.containsKey("Flicker") && compound.getByte("Flicker") == 1;
        boolean trail = compound.containsKey("Trail") && compound.getByte("Trail") == 1;
        FireworkEffectType type = compound.containsKey("Type") ? FireworkEffectType.byId(compound.getByte("Type")) : FireworkEffectType.SMALL_BALL;


        return new FireworkEffect(
                flicker,
                trail,
                type,
                primaryColor,
                secondaryColor);
    }

    public byte getFlicker() {
        return (byte) (this.flicker ? 1 : 0);
    }

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
        return new int[]{
                this.color.getRed(),
                this.color.getGreen(),
                this.color.getBlue()
        };
    }

    /**
     * Retrieves array of integer values corresponding to the fading colors of this firework's explosion.
     * <p>
     * Same handling of custom colors as Colors.
     *
     * @return An array of integer values corresponding to the fading colors of this firework's explosion.
     */
    public int[] getFadeColors() {
        return new int[]{
                this.fadeColor.getRed(),
                this.fadeColor.getGreen(),
                this.fadeColor.getBlue()
        };
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

}
