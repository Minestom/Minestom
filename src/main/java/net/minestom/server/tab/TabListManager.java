package net.minestom.server.tab;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TabListManager {

    private final Set<TabList> tablists = new CopyOnWriteArraySet<>();

    public void createTablist() {
        TabList tablist = new TabList();
        this.tablists.add(tablist);
    }

    public Set<TabList> getTablists() {
        return tablists;
    }
}
