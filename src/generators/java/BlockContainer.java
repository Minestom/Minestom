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
    private boolean hasBlockEntity;

    public BlockContainer(int ordinal, NamespaceID id, double hardness, double resistance, BlockState defaultState, List<BlockState> states) {
        this.ordinal = ordinal;
        this.id = id;
        this.hardness = hardness;
        this.resistance = resistance;
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

    public boolean isCoral() {
        return isCoral;
    }

    public boolean isFlowerPot() {
        return isFlowerPot;
    }

    public boolean isFlower() {
        return isFlower;
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

    public boolean hasBlockEntity() {
        return hasBlockEntity;
    }

    public BlockContainer setHasBlockEntity() {
        hasBlockEntity = true;
        return this;
    }

    public BlockContainer setCoral() {
        isCoral = true;
        return this;
    }

    public BlockContainer setFlowerPot() {
        isFlowerPot = true;
        return this;
    }

    public BlockContainer setFlower() {
        isFlower = true;
        return this;
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

    @Override
    public String toString() {
        return "BlockContainer{" +
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
                ", hasBlockEntity=" + hasBlockEntity +
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
