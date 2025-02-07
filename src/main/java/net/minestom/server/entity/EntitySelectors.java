package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntitySelector.Property;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.entity.EntitySelector.Target.ALL_ENTITIES;
import static net.minestom.server.entity.EntitySelector.Target.ALL_PLAYERS;
import static net.minestom.server.entity.EntitySelector.property;
import static net.minestom.server.entity.EntitySelector.selector;

public final class EntitySelectors {
    private static final EntitySelector<Entity> ALL = selector(s -> s.target(ALL_ENTITIES));
    private static final EntitySelector<Player> PLAYERS = selector(s -> s.target(ALL_PLAYERS));

    // Properties
    public static final Property<Entity, Integer> ID = property("id", Entity::getEntityId);
    public static final Property<Entity, UUID> UUID = property("uuid", Entity::getUuid);
    public static final Property<Entity, String> NAME = property("name", entity -> entity instanceof Player player ? player.getUsername() : null);
    public static final Property<Entity, Pos> POS = property("coord", Entity::getPosition);
    public static final Property<Entity, EntityType> TYPE = property("entity_type", Entity::getEntityType);
    public static final Property<Player, GameMode> GAME_MODE = property("game_mode", Player::getGameMode);
    public static final Property<Entity, Integer> LEVEL = property("experience", entity -> entity instanceof Player player ? player.getLevel() : 0);
    public static final Property<Entity, Float> EXPERIENCE = property("experience", entity -> entity instanceof Player player ? player.getExp() : 0f);

    public static @NotNull EntitySelector<Entity> all() {
        return ALL;
    }

    public static @NotNull EntitySelector<Player> players() {
        return PLAYERS;
    }
}
