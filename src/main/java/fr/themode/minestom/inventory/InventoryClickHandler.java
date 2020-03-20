package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;

public interface InventoryClickHandler {

    void leftClick(Player player, int slot);

    void rightClick(Player player, int slot);

    void shiftClick(Player player, int slot); // shift + left/right click have the same behavior

    void changeHeld(Player player, int slot, int key);

    void middleClick(Player player, int slot);

    void dropOne(Player player, int slot);

    void dropItemStack(Player player, int slot);

    void dragging(Player player, int slot, int button);

    void doubleClick(Player player, int slot);

}
