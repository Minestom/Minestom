package net.minestom.server.advancements;

import net.minestom.server.utils.validate.Check;

import java.util.HashMap;
import java.util.Map;

public class AdvancementManager {

    private Map<String, AdvancementTab> advancementTabMap = new HashMap<>();

    public AdvancementTab createTab(String rootIdentifier, AdvancementRoot root) {
        Check.stateCondition(advancementTabMap.containsKey(rootIdentifier),
                "A tab with the identifier '" + rootIdentifier + "' already exists");
        final AdvancementTab advancementTab = new AdvancementTab(rootIdentifier, root);
        this.advancementTabMap.put(rootIdentifier, advancementTab);
        return advancementTab;
    }

    public AdvancementTab getTab(String rootIdentifier) {
        return advancementTabMap.get(rootIdentifier);
    }

}
