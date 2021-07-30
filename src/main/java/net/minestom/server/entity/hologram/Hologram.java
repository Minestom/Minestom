package net.minestom.server.entity.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents an invisible armor stand showing a {@link Component}.
 */
public class Hologram implements Viewable {

    private static final float OFFSET_Y = -0.9875f;
    private static final float MARKER_OFFSET_Y = -0.40625f;

    private final Entity entity;
    private final float yOffset;

    private Pos position;
    private Component text;

    private boolean removed;

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     */
    public Hologram(Instance instance, Pos spawnPosition, Component text) {
        this(instance, spawnPosition, text, true);
    }

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  {@code true}if the hologram should be visible automatically, otherwise {@code false}.
     */
    public Hologram(Instance instance, Pos spawnPosition, Component text, boolean autoViewable) {
        this(instance, spawnPosition, text, autoViewable, false);
    }

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  {@code true}if the hologram should be visible automatically, otherwise {@code false}.
     */
    public Hologram(Instance instance, Pos spawnPosition, Component text, boolean autoViewable, boolean marker) {
        this.entity = new Entity(EntityType.ARMOR_STAND);

        ArmorStandMeta armorStandMeta = (ArmorStandMeta) entity.getEntityMeta();

        armorStandMeta.setNotifyAboutChanges(false);

        if (marker) {
            this.yOffset = MARKER_OFFSET_Y;
            armorStandMeta.setMarker(true);
        } else {
            this.yOffset = OFFSET_Y;
            armorStandMeta.setSmall(true);
        }
        armorStandMeta.setHasNoGravity(true);
        armorStandMeta.setCustomName(Component.empty());
        armorStandMeta.setCustomNameVisible(true);
        armorStandMeta.setInvisible(true);

        armorStandMeta.setNotifyAboutChanges(true);

        this.entity.setInstance(instance, spawnPosition.add(0, this.yOffset, 0));
        this.entity.setAutoViewable(autoViewable);

        this.position = spawnPosition;
        setText(text);
    }

    /**
     * Gets the position of the hologram.
     *
     * @return the hologram's position
     */
    public Pos getPosition() {
        return position;
    }

    /**
     * Changes the position of the hologram.
     *
     * @param position the new hologram's position
     */
    public void setPosition(Pos position) {
        checkRemoved();
        this.position = position.add(0, this.yOffset, 0);
        this.entity.teleport(this.position);
    }

    /**
     * Gets the hologram text.
     *
     * @return the hologram text
     */
    public Component getText() {
        return text;
    }

    /**
     * Changes the hologram text.
     *
     * @param text the new hologram text
     */
    public void setText(Component text) {
        checkRemoved();
        this.text = text;
        this.entity.setCustomName(text);
    }

    /**
     * Removes the hologram.
     */
    public void remove() {
        this.removed = true;
        this.entity.remove();
    }

    /**
     * Checks if the hologram is still present.
     *
     * @return true if the hologram is present, false otherwise
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Gets the hologram entity (armor stand).
     *
     * @return the hologram entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addViewer(@NotNull Player player) {
        return entity.addViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeViewer(@NotNull Player player) {
        return entity.removeViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Set<Player> getViewers() {
        return entity.getViewers();
    }

    /**
     * @see #isRemoved()
     */
    private void checkRemoved() {
        Check.stateCondition(isRemoved(), "You cannot interact with a removed Hologram");
    }
}
