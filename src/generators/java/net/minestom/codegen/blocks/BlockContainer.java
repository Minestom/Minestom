package net.minestom.codegen.blocks;

import net.minestom.server.utils.NamespaceID;

import java.util.List;
import java.util.Map;

public class BlockContainer implements Comparable<BlockContainer> {

    private int ordinal;
    private NamespaceID id;
    private double hardness;
    private double resistance;
    private BlockState defaultState;
    private boolean isSolid;
    private boolean isAir;
    private List<BlockState> states;

    private boolean isMushroom;
    private boolean isLiquid;
    private boolean isFlower;
    private boolean isFlowerPot;
    private boolean isCoral;
    private NamespaceID blockEntity;

    public BlockContainer(int ordinal, NamespaceID id, double hardness, double resistance, NamespaceID blockEntity, BlockState defaultState, List<BlockState> states) {
        this.ordinal = ordinal;
        this.id = id;
        this.hardness = hardness;
        this.resistance = resistance;
        this.blockEntity = blockEntity;
        this.defaultState = defaultState;
        this.states = states;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public BlockState getDefaultState() {
        return defaultState;
    }

    public List<BlockState> getStates() {
        return states;
    }

    public NamespaceID getId() {
        return id;
    }

    public boolean isAir() {
        return isAir;
    }

    public boolean isLiquid() {
        return isLiquid;
    }

    public boolean isMushroom() {
        return isMushroom;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public double getHardness() {
        return hardness;
    }

    public double getResistance() {
        return resistance;
    }

    public NamespaceID getBlockEntityName() {
        return blockEntity;
    }

    public BlockContainer setLiquid() {
        isLiquid = true;
        return this;
    }

    public BlockContainer setMushroom() {
        isMushroom = true;
        return this;
    }

    public BlockContainer setSolid() {
        isSolid = true;
        return this;
    }

    public BlockContainer setAir() {
        isAir = true;
        return this;
    }

    @Override
    public String toString() {
        return "blocks.BlockContainer{" +
                "id=" + id +
                ", hardness=" + hardness +
                ", resistance=" + resistance +
                ", defaultState=" + defaultState +
                ", isSolid=" + isSolid +
                ", isAir=" + isAir +
                ", states=" + states +
                ", isMushroom=" + isMushroom +
                ", isLiquid=" + isLiquid +
                ", isFlower=" + isFlower +
                ", isFlowerPot=" + isFlowerPot +
                ", isCoral=" + isCoral +
                ", blockEntity=" + blockEntity +
                '}';
    }

    @Override
    public int compareTo(BlockContainer o) {
        return Integer.compare(ordinal, o.ordinal);
    }

    public static class BlockState {
        private short id;
        private Map<String, String> properties;

        public BlockState(short id, Map<String, String> properties) {
            this.id = id;
            this.properties = properties;
        }

        public short getId() {
            return id;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return "BlockState{" +
                    "id=" + id +
                    ", properties=" + properties +
                    '}';
        }
    }

}
