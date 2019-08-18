package fr.themode.minestom.instance;

public class Block {

    private short type;

    public Block(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("CustomBlock{type=%s}", type);
    }
}
