package net.minestom.server.ui;

public sealed interface TabList permits TabListImpl {

    static TabListBuilder builder() {
        return new TabListBuilder();
    }

}
