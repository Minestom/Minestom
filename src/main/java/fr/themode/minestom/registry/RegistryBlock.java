package fr.themode.minestom.registry;

import java.util.ArrayList;
import java.util.List;

public class RegistryBlock {

    protected String name;

    protected List<String> propertiesIdentifiers = new ArrayList<>();

    protected List<String> defaultPropertiesValues = new ArrayList<>();
    protected short defaultId;

    protected List<BlockState> states = new ArrayList<>();

    public static class BlockState {

        protected List<String> propertiesValues = new ArrayList<>();
        protected short id;
        protected boolean isDefault;

    }

}
