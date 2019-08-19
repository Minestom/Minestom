package fr.themode.minestom.instance;

public interface BlockModifier {

    void setBlock(int x, int y, int z, short blockId);

    void setBlock(int x, int y, int z, String blockId);
}
