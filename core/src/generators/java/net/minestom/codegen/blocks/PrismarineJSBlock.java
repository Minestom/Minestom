package net.minestom.codegen.blocks;

public class PrismarineJSBlock {

    int id;
    String name;
    double hardness;
    boolean diggable;
    boolean transparent;
    int emitLight;
    int filterLight;
    String boundingBox;
    int stackSize;
    String material;

    @Override
    public String toString() {
        return "blocks.PrismarineJSBlock{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", hardness=" + hardness +
                ", diggable=" + diggable +
                ", transparent=" + transparent +
                ", emitLight=" + emitLight +
                ", filterLight=" + filterLight +
                ", boundingBox='" + boundingBox + '\'' +
                ", stackSize=" + stackSize +
                ", material='" + material + '\'' +
                '}';
    }

    /*
    TODO:
    "harvestTools": {
      "521": true,
      "535": true,
      "539": true,
      "543": true,
      "550": true
    }
    */
}
