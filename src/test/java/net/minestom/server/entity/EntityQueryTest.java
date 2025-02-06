package net.minestom.server.entity;

import static net.minestom.server.entity.EntityQuery.Condition.equalsCondition;
import static net.minestom.server.entity.EntityQuery.Condition.rangeCondition;
import static net.minestom.server.entity.EntityQuery.*;

import net.minestom.server.utils.Range;
import org.junit.jupiter.api.Test;

public final class EntityQueryTest {

    @Test
    public void nameQuery() {
        var query = entityQuery(
                equalsCondition(NAME, "TheMode")
        );
    }

    @Test
    public void distanceRange() {
        var query = entityQuery(
                rangeCondition(DISTANCE, new Range.Double(5, 10))
        );
    }
}
