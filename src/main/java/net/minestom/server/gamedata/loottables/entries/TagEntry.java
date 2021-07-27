package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class TagEntry extends LootTable.Entry {
    private final Tag tag;
    private final boolean expand;
    private Random rng = new Random();

    TagEntry(TagType type, Tag tag, boolean expand, int weight, int quality, List<Condition> conditions) {
        super(type, weight, quality, conditions);
        this.tag = tag;
        this.expand = expand;
    }

    @Override
    public void generate(List<ItemStack> output, Data arguments) {
        Set<NamespaceID> values = tag.getValues();
        if (values.isEmpty())
            return;
        Material[] asArrayOfItems = new Material[values.size()];
        int ptr = 0;
        for (NamespaceID id : values) {
            asArrayOfItems[ptr++] = Material.fromNamespaceId(id);
        }
        if (expand) {
            Material selectedMaterial = asArrayOfItems[rng.nextInt(asArrayOfItems.length)];
            output.add(ItemStack.of(selectedMaterial));
        } else {
            for (Material material : asArrayOfItems) {
                output.add(ItemStack.of(material));
            }
        }
    }
}
