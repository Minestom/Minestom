package fr.themode.minestom.chat;

import fr.themode.minestom.utils.HexUtils;

public enum ChatColor {

    BLACK((byte) 0),
    DARK_BLUE((byte) 1),
    DARK_GREEN((byte) 2),
    DARK_AQUA((byte) 3),
    DARK_RED((byte) 4),
    DARK_PURPLE((byte) 5),
    GOLD((byte) 6),
    GRAY((byte) 7),
    DARK_GRAY((byte) 8),
    BLUE((byte) 9),
    GREEN((byte) 0xa),
    AQUA((byte) 0xb),
    RED((byte) 0xc),
    LIGHT_PURPLE((byte) 0xd),
    YELLOW((byte) 0xe),
    WHITE((byte) 0xf);


    private byte id;

    ChatColor(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    @Override
    public String toString() {
        return Chat.COLOR_CHAR + String.valueOf(HexUtils.byteToHex(id));
    }
}
