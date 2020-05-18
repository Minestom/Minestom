package net.minestom.server.gamedata.loottables;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.item.ItemStack;

import java.util.Collection;

/**
 * Loot table function that must meet some conditions to be applied
 */
public class ConditionedFunctionWrapper implements LootTableFunction {

    private final LootTableFunction baseFunction;
    private final Collection<Condition> conditions;

    public ConditionedFunctionWrapper(LootTableFunction baseFunction, Collection<Condition> conditions) {
        this.baseFunction = baseFunction;
        this.conditions = conditions;
    }

    @Override
    public ItemStack apply(ItemStack stack, Data data) {
        for (Condition c : conditions) {
            if(!c.test(data))
                return stack;
        }
        return baseFunction.apply(stack, data);
    }
}
