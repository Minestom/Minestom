package net.minestom.server.entity.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * A hologram that sets the marker flag of the Armor Stand, allowing players to place blocks inside of it
 */
public class MarkerHologram extends Hologram {

    //Y Offset such that the spawnPosition represents the center of the rendered nametag
    private static final float OFFSET_Y = -0.40625f;

    /**
     * Constructs a new {@link MarkerHologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  {@code true}if the hologram should be visible automatically, otherwise {@code false}.
     * @deprecated Use {@link #MarkerHologram(Instance, Position, Component, boolean)}
     */
    public MarkerHologram(Instance instance, Position spawnPosition, JsonMessage text, boolean autoViewable) {
        super(instance, spawnPosition, text, autoViewable);
    }

    /**
     * Constructs a new {@link MarkerHologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @deprecated Use {@link #MarkerHologram(Instance, Position, Component)}
     */
    public MarkerHologram(Instance instance, Position spawnPosition, JsonMessage text) {
        super(instance, spawnPosition, text);
    }

    /**
     * Constructs a new {@link MarkerHologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     */
    public MarkerHologram(Instance instance, Position spawnPosition, Component text) {
        super(instance, spawnPosition, text);
    }

    /**
     * Constructs a new {@link MarkerHologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  {@code true}if the hologram should be visible automatically, otherwise {@code false}.
     */
    public MarkerHologram(Instance instance, Position spawnPosition, Component text, boolean autoViewable) {
        super(instance, spawnPosition, text, autoViewable);
    }

    /**
     * Sets the default {@link ArmorStandMeta} flags for this Hologram,
     * subclasses may override this method to modify the metadata.
     *
     * {@link MarkerHologram}: Set the marker flag to true
     *
     * @param armorStandMeta the meta to update
     */
    @Override
    protected void updateDefaultMeta(ArmorStandMeta armorStandMeta) {
        super.updateDefaultMeta(armorStandMeta);

        armorStandMeta.setMarker(true);
    }

    /**
     * Vertical offset used to center the nametag,
     * subclasses may override this method to modify the position
     *
     * {@link MarkerHologram}: Correct the Y offset for marker Armor Stands
     *
     * @return
     */
    @Override
    protected float getOffsetY() {
        return OFFSET_Y;
    }
}

