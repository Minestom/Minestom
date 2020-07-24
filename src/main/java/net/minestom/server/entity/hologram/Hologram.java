package net.minestom.server.entity.hologram;

import net.minestom.server.Viewable;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.type.EntityArmorStand;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.validate.Check;

import java.util.Set;

public class Hologram implements Viewable {

    private static final float OFFSET_Y = -0.9875f;

    private HologramEntity entity;

    private Position position;
    private ColoredText text;

    private boolean removed;

    public Hologram(Instance instance, Position spawnPosition, ColoredText text, boolean autoViewable) {
        this.entity = new HologramEntity(spawnPosition.clone().add(0, OFFSET_Y, 0));
        this.entity.setInstance(instance);
        this.entity.setAutoViewable(autoViewable);

        this.position = spawnPosition;
        setText(text);
    }

    public Hologram(Instance instance, Position spawnPosition, ColoredText text) {
        this(instance, spawnPosition, text, true);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        checkRemoved();
        position = position.add(0, OFFSET_Y, 0);
        this.position = position;
        this.entity.teleport(position);
    }

    public ColoredText getText() {
        return text;
    }

    public void setText(ColoredText text) {
        checkRemoved();
        this.text = text;
        this.entity.setCustomName(text);
    }

    public void remove() {
        this.removed = true;
        this.entity.remove();
    }

    public boolean isRemoved() {
        return removed;
    }

    /**
     * Get the hologram entity (armor stand)
     *
     * @return the hologram entity
     */
    public HologramEntity getEntity() {
        return entity;
    }

    @Override
    public boolean addViewer(Player player) {
        return entity.addViewer(player);
    }

    @Override
    public boolean removeViewer(Player player) {
        return entity.removeViewer(player);
    }

    @Override
    public Set<Player> getViewers() {
        return entity.getViewers();
    }

    private void checkRemoved() {
        Check.stateCondition(isRemoved(), "You cannot interact with a removed Hologram");
    }


    private class HologramEntity extends EntityArmorStand {

        public HologramEntity(Position spawnPosition) {
            super(spawnPosition);
            setSmall(true);

            setNoGravity(true);
            setCustomName(ColoredText.of(""));
            setCustomNameVisible(true);
            setInvisible(true);
        }

    }
}
