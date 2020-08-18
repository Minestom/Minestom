package net.minestom.server.advancements;

import net.minestom.server.utils.validate.Check;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to manages advancement tabs
 */
public class AdvancementManager {

    private final Map<String, AdvancementTab> advancementTabMap = new HashMap<>();

    /**
     * Create a new tab with a single advancement
     *
     * @param rootIdentifier the root identifier
     * @param root           the root advancement
     * @return the {@link AdvancementTab} created
     * @throws IllegalStateException if a tab with the identifier {@code rootIdentifier} already exists
     */
    public AdvancementTab createTab(String rootIdentifier, AdvancementRoot root) {
        Check.stateCondition(advancementTabMap.containsKey(rootIdentifier),
                "A tab with the identifier '" + rootIdentifier + "' already exists");
        final AdvancementTab advancementTab = new AdvancementTab(rootIdentifier, root);
        this.advancementTabMap.put(rootIdentifier, advancementTab);
        return advancementTab;
    }

    /**
     * Get an advancement tab by its root identifier
     *
     * @param rootIdentifier the root identifier of the tab
     * @return the {@link AdvancementTab} associated with the identifer, null if not any
     */
    public AdvancementTab getTab(String rootIdentifier) {
        return advancementTabMap.get(rootIdentifier);
    }

    /**
     * Get all the created tab
     *
     * @return the collection containing all created {@link AdvancementTab}
     */
    public Collection<AdvancementTab> getTabs() {
        return advancementTabMap.values();
    }

}
