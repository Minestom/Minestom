package net.minestom.server.gamedata;

import net.minestom.server.data.Data;

/**
 * Represents a condition, used by predicates in MC functions and in loot tables.
 */
@FunctionalInterface
public interface Condition {

    /**
     * Tests this condition. Subclasses are free to throw runtime exceptions if the arguments passed through data are not valid or missing
     * @param data arguments to give to the condition. May be null if the condition supports it
     * @return 'true' if the condition passed, 'false' otherwise
     */
    boolean test(Data data);

    Condition ALWAYS_YES = (_d) -> true;
    Condition ALWAYS_NO = (_d) -> false;
}
