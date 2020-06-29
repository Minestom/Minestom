package net.minestom.codegen.blocks;

public class BurgerBlock {

    String text_id;
    double resistance;

    // from tileentities
    BurgerTileEntity blockEntity;

    @Override
    public String toString() {
        return "blocks.BurgerBlock{" +
                "text_id='" + text_id + '\'' +
                ", resistance=" + resistance +
                '}';
    }
}
