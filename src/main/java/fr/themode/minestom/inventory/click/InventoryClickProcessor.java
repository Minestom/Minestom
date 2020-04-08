package fr.themode.minestom.inventory.click;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.condition.InventoryCondition;
import fr.themode.minestom.inventory.condition.InventoryConditionResult;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.StackingRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InventoryClickProcessor {

    // Dragging maps
    private Map<Player, Set<Integer>> leftDraggingMap = new HashMap<>();
    private Map<Player, Set<Integer>> rightDraggingMap = new HashMap<>();

    public InventoryClickResult leftClick(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventoryCondition, player, slot, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        StackingRule cursorRule = cursor.getStackingRule();
        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorRule.canBeStacked(cursor, clicked)) {

            resultCursor = cursor.clone();
            resultClicked = clicked.clone();

            int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);

            if (!clickedRule.canApply(resultClicked, totalAmount)) {
                resultCursor = cursorRule.apply(resultCursor, totalAmount - cursorRule.getMaxSize());
                resultClicked = clickedRule.apply(resultClicked, clickedRule.getMaxSize());
            } else {
                resultCursor = cursorRule.apply(resultCursor, 0);
                resultClicked = clickedRule.apply(resultClicked, totalAmount);
            }
        } else {
            resultCursor = clicked.clone();
            resultClicked = cursor.clone();
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    public InventoryClickResult rightClick(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventoryCondition, player, slot, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        StackingRule cursorRule = cursor.getStackingRule();
        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (clickedRule.canBeStacked(clicked, cursor)) {
            resultClicked = clicked.clone();
            int amount = clicked.getAmount() + 1;
            if (!clickedRule.canApply(resultClicked, amount)) {
                return clickResult;
            } else {
                resultCursor = cursor.clone();
                resultCursor = cursorRule.apply(resultCursor, cursorRule.getAmount(resultCursor) - 1);
                resultClicked = clickedRule.apply(resultClicked, amount);
            }
        } else {
            if (cursor.isAir()) {
                int amount = (int) Math.ceil((double) clicked.getAmount() / 2d);
                resultCursor = clicked.clone();
                resultCursor = cursorRule.apply(resultCursor, amount);

                resultClicked = clicked.clone();
                resultClicked = clickedRule.apply(resultClicked, clicked.getAmount() / 2);
            } else {
                if (clicked.isAir()) {
                    int amount = cursor.getAmount();
                    resultCursor = cursor.clone();
                    resultCursor = cursorRule.apply(resultCursor, amount - 1);

                    resultClicked = cursor.clone();
                    resultClicked = clickedRule.apply(resultClicked, 1);
                } else {
                    resultCursor = clicked.clone();
                    resultClicked = cursor.clone();
                }
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    public InventoryClickResult changeHeld(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventoryCondition, player, slot, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        StackingRule cursorRule = cursor.getStackingRule();
        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultClicked;
        ItemStack resultHeld;

        if (clicked.isAir()) {
            // Set held item [key] to slot
            resultClicked = ItemStack.AIR_ITEM;
            resultHeld = clicked.clone();
        } else {
            if (cursor.isAir()) {
                // if held item [key] is air then set clicked to held
                resultClicked = ItemStack.AIR_ITEM;
                resultHeld = clicked.clone();
            } else {
                // Otherwise replace held item and held
                resultClicked = cursor.clone();
                resultHeld = clicked.clone();
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultHeld);

        return clickResult;
    }

    private InventoryClickResult startCondition(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursor);
            inventoryCondition.accept(player, slot, result);

            cursor = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                clickResult.setClicked(clicked);
                clickResult.setCursor(cursor);
                clickResult.setRefresh(true);
            }
        }
        return clickResult;
    }

}
