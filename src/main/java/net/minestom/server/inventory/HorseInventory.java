package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.OpenHorseWindowPacket;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a horse inventory which can be viewed by a collection of {@link Player}.
 * Please make sure the entity is a horse-like entity (horse, donkey, mule, skeleton horse or zombie horse).
 * <p>
 * It can then be opened using {@link Player#openInventory(ViewableInventory)}.
 */
public non-sealed class HorseInventory extends ViewableInventory {
    private final Entity entity;
    private final int columns;

    public HorseInventory(Entity entity, int columns) {
        super(columns * 3 + 2); // 3 rows + 2 slots for horse armor and saddle
        this.entity = entity;
        this.columns = columns;
    }

    @Override
    SendablePacket getOpenPacket() {
        return new OpenHorseWindowPacket(id, columns, entity.getEntityId());
    }
}
