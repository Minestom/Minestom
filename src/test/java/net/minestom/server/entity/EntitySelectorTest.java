package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static net.minestom.server.entity.EntitySelectors.NAME;
import static net.minestom.server.entity.EntitySelectors.TYPE;

public final class EntitySelectorTest {

    @Test
    public void playerQuery() {
        EntitySelector<Player> selectorPlayer = EntitySelector.selector(builder -> {
            builder.requirePlayer();
            builder.predicate(NAME, (point, o) -> o.equals("TheMode"));
            builder.predicateEquals(TYPE, EntityType.PLAYER);
            builder.limit(5);
        });
    }
}
