package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.PlayerSkin;

import java.util.List;

/*package-private*/ record TabListImpl(
        Component header,
        Component footer,
        boolean hasPlayerList,
        List<Component> beforeText,
        List<PlayerSkin> beforeSkin,
        List<Component> afterText,
        List<PlayerSkin> afterSkin
) implements TabList {

}
