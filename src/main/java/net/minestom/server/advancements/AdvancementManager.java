package net.minestom.server.advancements;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to manage all the registered {@link AdvancementTab}.
 * <p>
 * Use {@link #createTab(String, AdvancementRoot)} to create a tab with the appropriate {@link AdvancementRoot}.
 */
public class AdvancementManager {

    // root identifier = its advancement tab
    private final Map<String, AdvancementTab> advancementTabMap = new ConcurrentHashMap<>();

    /**
     * Creates a new {@link AdvancementTab} with a single {@link AdvancementRoot}.
     *
     * @param rootIdentifier the root identifier
     * @param root           the root advancement
     * @return the newly created {@link AdvancementTab}
     * @throws IllegalStateException if a tab with the identifier {@code rootIdentifier} already exists
     */
    @NotNull
    public AdvancementTab createTab(@NotNull String rootIdentifier, @NotNull AdvancementRoot root) {
        Check.stateCondition(advancementTabMap.containsKey(rootIdentifier),
                "A tab with the identifier '" + rootIdentifier + "' already exists");
        final AdvancementTab advancementTab = new AdvancementTab(rootIdentifier, root);
        this.advancementTabMap.put(rootIdentifier, advancementTab);
        return advancementTab;
    }

    /**
     * Gets an advancement tab by its root identifier.
     *
     * @param rootIdentifier the root identifier of the tab
     * @return the {@link AdvancementTab} associated with the identifier, null if not any
     */
    @Nullable
    public AdvancementTab getTab(@NotNull String rootIdentifier) {
        return advancementTabMap.get(rootIdentifier);
    }

    /**
     * Gets all the created {@link AdvancementTab}.
     *
     * @return the collection containing all created {@link AdvancementTab}
     */
    @NotNull
    public Collection<AdvancementTab> getTabs() {
        return advancementTabMap.values();
    }

}
