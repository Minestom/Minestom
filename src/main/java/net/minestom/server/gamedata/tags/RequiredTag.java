package net.minestom.server.gamedata.tags;

import net.minestom.server.utils.NamespaceID;

public class RequiredTag {
    private final Tag.BasicTypes type;
    private final NamespaceID name;

    public RequiredTag(Tag.BasicTypes type, NamespaceID name) {
        this.type = type;
        this.name = name;
    }

    public NamespaceID getName() {
        return name;
    }

    public Tag.BasicTypes getType() {
        return type;
    }
}
