package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntitySelector.Property;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.entity.EntitySelector.Builder;
import static net.minestom.server.entity.EntitySelector.property;

public final class EntitySelectors {
    private static final EntitySelector<Entity> ALL = EntitySelector.selector(builder -> {});
    private static final EntitySelector<Player> PLAYERS = EntitySelector.selector(Builder::requirePlayer);

    // Properties
    public static final Property<Entity, Integer> ID = property("id", Entity::getEntityId);
    public static final Property<Entity, UUID> UUID = property("uuid", Entity::getUuid);
    public static final Property<Player, String> NAME = property("name", Player::getUsername);
    public static final Property<Entity, Pos> POS = property("coord", Entity::getPosition);
    public static final Property<Entity, EntityType> TYPE = property("entity_type", Entity::getEntityType);
    public static final Property<Player, GameMode> GAME_MODE = property("game_mode", Player::getGameMode);
    public static final Property<Player, Integer> LEVEL = property("experience", Player::getLevel);
    public static final Property<Player, Float> EXPERIENCE = property("experience", Player::getExp);

    public static @NotNull EntitySelector<Entity> all() {
        return ALL;
    }

    public static @NotNull EntitySelector<Player> players() {
        return PLAYERS;
    }
}
