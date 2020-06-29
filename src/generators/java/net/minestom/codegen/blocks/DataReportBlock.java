package net.minestom.codegen.blocks;

import com.google.gson.annotations.SerializedName;
import net.minestom.server.utils.NamespaceID;

import java.util.Arrays;
import java.util.Map;

public class DataReportBlock {

    NamespaceID name;
    Map<String, String[]> properties;
    BlockState[] states;

    BlockState defaultState;

    /**
     * Looks for the first state in {@link #states} with #isDefault set and stores it into {@link #defaultState}
     */
    public void bindDefaultState() {
        for(BlockState s : states) {
            if(s.isDefault) {
                defaultState = s;
                return;
            }
        }
    }

    public static class BlockState {
        protected Map<String, String> properties;
        protected short id;
        @SerializedName("default")
        protected boolean isDefault;

        @Override
        public String toString() {
            return "BlockState{" +
                    "properties=" + properties +
                    ", id=" + id +
                    ", isDefault=" + isDefault +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "blocks.DataReportBlock{" +
                "name=" + name +
                ", properties=" + properties +
                ", states=" + Arrays.toString(states) +
                ", defaultState=" + defaultState +
                '}';
    }
}
