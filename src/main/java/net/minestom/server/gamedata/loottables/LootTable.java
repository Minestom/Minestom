package net.minestom.server.gamedata.loottables;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.WeightedRandom;
import net.minestom.server.utils.WeightedRandomItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class LootTable {

    public static final String LUCK_KEY = "minecraft:luck";

    private final LootTableType type;
    private final List<LootTable.Pool> pools;

    public LootTable(LootTableType type, List<Pool> pools) {
        this.type = type;
        this.pools = pools;
    }

    public LootTableType getType() {
        return type;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public List<ItemStack> generate(Data arguments) {
        if(arguments == null)
            arguments = Data.EMPTY;
        List<ItemStack> output = new LinkedList<>();
        for(Pool p : pools) {
            p.generate(output, arguments);
        }
        return output;
    }

    public static class Pool {
        private final int minRollCount;
        private final int maxRollCount;
        private final int bonusMinRollCount;
        private final int bonusMaxRollCount;
        private final List<LootTable.Entry> entries;
        private final List<Condition> conditions;

        public Pool(int minRollCount, int maxRollCount, int bonusMinRollCount, int bonusMaxRollCount, List<Entry> entries, List<Condition> conditions) {
            this.minRollCount = minRollCount;
            this.maxRollCount = maxRollCount;
            this.bonusMinRollCount = bonusMinRollCount;
            this.bonusMaxRollCount = bonusMaxRollCount;
            this.entries = entries;
            this.conditions = conditions;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public int getMinRollCount() {
            return minRollCount;
        }

        public int getMaxRollCount() {
            return maxRollCount;
        }

        public List<Entry> getEntries() {
            return entries;
        }

        public void generate(List<ItemStack> output, Data arguments) {
            for(Condition c : conditions) {
                if(!c.test(arguments))
                    return;
            }
            Random rng = new Random();
            int luck = arguments.getOrDefault(LUCK_KEY, 0);
            int rollCount = rng.nextInt(maxRollCount - minRollCount +1 /*inclusive*/) + minRollCount;
            int bonusRollCount = rng.nextInt(bonusMaxRollCount - bonusMinRollCount +1 /*inclusive*/) + bonusMinRollCount;
            bonusRollCount *= luck;
            // TODO: implement luck (quality/weight) weight=floor( weight + (quality * generic.luck))
            WeightedRandom<Entry> weightedRandom = new WeightedRandom<>(entries);
            for (int i = 0; i < rollCount+bonusRollCount; i++) {
                Entry entry = weightedRandom.get(rng);
                entry.generateStacks(output, arguments);
            }
        }
    }

    public abstract static class Entry implements WeightedRandomItem {
        private final LootTableEntryType type;
        private final int weight;
        private final int quality;
        private final List<Condition> conditions;

        public Entry(LootTableEntryType type, int weight, int quality, List<Condition> conditions) {
            this.type = type;
            this.weight = weight;
            this.quality = quality;
            this.conditions = conditions;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public int getQuality() {
            return quality;
        }

        public double getWeight() {
            return weight;
        }

        public LootTableEntryType getType() {
            return type;
        }

        public final void generateStacks(List<ItemStack> output, Data arguments) {
            for(Condition c : conditions) {
                if(!c.test(arguments))
                    return;
            }
            generate(output, arguments);
        }

        protected abstract void generate(List<ItemStack> output, Data arguments);
    }
}
