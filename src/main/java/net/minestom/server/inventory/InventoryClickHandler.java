package net.minestom.server.inventory;

import net.minestom.server.entity.Player;

public interface InventoryClickHandler {

    void leftClick(Player player, int slot);

    void rightClick(Player player, int slot);

    void shiftClick(Player player, int slot); // shift + left/right click have the same behavior

    void changeHeld(Player player, int slot, int key);

    void middleClick(Player player, int slot);

    void drop(Player player, int mode, int slot, int button);

    void dragging(Player player, int slot, int button);

    void doubleClick(Player player, int slot);

}
