package net.minestom.server.gamedata.loottables;

import net.minestom.server.gamedata.Condition;
import net.minestom.server.utils.NamespaceID;

import java.util.LinkedList;
import java.util.List;

/**
 * Meant only for parsing loot tables
 */
class LootTableContainer {


    private String type;
    private LootTableContainer.Pool[] pools;

    private LootTableContainer() {}

    public LootTable createTable(LootTableManager lootTableManager) {
        LootTableType type = lootTableManager.getTableType(NamespaceID.from(this.type));
        List<LootTable.Pool> pools = new LinkedList<>();
        if(this.pools != null) {
            for(Pool p : this.pools) {
                pools.add(p.create(lootTableManager));
            }
        }
        return new LootTable(type, pools);
    }

    private class Pool {
        private ConditionContainer[] conditions;
        private FunctionContainer[] functions;
        private RangeContainer rolls;
        private RangeContainer bonus_rools;

        private Entry[] entries;

        private Pool() {}

        public LootTable.Pool create(LootTableManager lootTableManager) {
            List<LootTable.Entry> entries = new LinkedList<>();
            List<Condition> conditions = new LinkedList<>();
            if(this.entries != null) {
                for (Entry e : this.entries) {
                    entries.add(e.create(lootTableManager));
                }
            }
            if(this.conditions != null) {
                for (ConditionContainer c : this.conditions) {
                    conditions.add(c.create(lootTableManager));
                }
            }
            if(rolls == null)
                rolls = new RangeContainer(0,0);
            if(bonus_rools == null)
                bonus_rools = new RangeContainer(0,0);
            return new LootTable.Pool(rolls.getMin(), rolls.getMax(), bonus_rools.getMin(), bonus_rools.getMax(), entries, conditions);
        }
    }

    private class Entry {
        private ConditionContainer[] conditions;
        private String type;
        private String name;
        private Entry[] children;
        private boolean expand;
        private FunctionContainer[] functions;
        private int weight;
        private int quality;

        private Entry() {}

        public LootTable.Entry create(LootTableManager lootTableManager) {
            LootTableEntryType entryType = lootTableManager.getEntryType(NamespaceID.from(type));
            List<Condition> conditions = new LinkedList<>();
            if(this.conditions != null) {
                for(ConditionContainer c : this.conditions) {
                    conditions.add(c.create(lootTableManager));
                }
            }
            List<LootTable.Entry> children = new LinkedList<>();
            if(this.children != null) {
                for (Entry c : this.children) {
                    children.add(c.create(lootTableManager));
                }
            }
            List<LootTableFunction> functions = new LinkedList<>();
            if(this.functions != null) {
                for(FunctionContainer c : this.functions) {
                    functions.add(c.create(lootTableManager));
                }
            }
            return entryType.create(lootTableManager, name, conditions, children, expand, functions, weight, quality);
        }
    }

    private static class FunctionContainer {
        private String function;
        private ConditionContainer[] conditions;

        private FunctionContainer() {}

        public LootTableFunction create(LootTableManager lootTableManager) {
            List<Condition> conditions = new LinkedList<>();
            if(this.conditions != null) {
                for(ConditionContainer c : this.conditions) {
                    conditions.add(c.create(lootTableManager));
                }
            }
            return new ConditionedFunctionWrapper(lootTableManager.getFunction(NamespaceID.from(function)), conditions);
        }
    }

}
