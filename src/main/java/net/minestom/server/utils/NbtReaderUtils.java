package net.minestom.server.utils;

import net.kyori.text.Component;
import net.minestom.server.chat.Chat;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;

import java.util.ArrayList;

public class NbtReaderUtils {

    public static void readItemStackNBT(PacketReader reader, ItemStack item) {

        byte typeId = reader.readByte();

        //System.out.println("DEBUG TYPE: " + typeId);
        switch (typeId) {
            case 0x00: // TAG_End
                // End of item NBT
                return;
            case 0x01: // TAG_Byte

                break;
            case 0x02: // TAG_Short
                String shortName = reader.readShortSizedString();

                // Damage NBT
                if (shortName.equals("Damage")) {
                    short damage = reader.readShort();
                    item.setDamage(damage);
                    readItemStackNBT(reader, item);
                }
                break;
            case 0x03: // TAG_Int
                String intName = reader.readShortSizedString();

                // Damage
                if (intName.equals("Damage")) {
                    int damage = reader.readInteger();
                    //item.setDamage(damage);
                    // TODO short vs int damage
                    readItemStackNBT(reader, item);
                }

                // Unbreakable
                if (intName.equals("Unbreakable")) {
                    int value = reader.readInteger();
                    item.setUnbreakable(value == 1);
                    readItemStackNBT(reader, item);
                }
                break;
            case 0x04: // TAG_Long

                break;
            case 0x05: // TAG_Float

                break;
            case 0x06: // TAG_Double

                break;
            case 0x07: // TAG_Byte_Array

                break;
            case 0x08: // TAG_String

                break;
            case 0x09: // TAG_List

                String listName = reader.readShortSizedString();

                if (listName.equals("StoredEnchantments")) {
                    reader.readByte(); // Should be a compound (0x0A)
                    int size = reader.readInteger();

                    for (int ench = 0; ench < size; ench++) {
                        byte test = reader.readByte();
                        String lvlName = reader.readShortSizedString();
                        short lvl = reader.readShort();

                        byte test2 = reader.readByte();
                        String idName = reader.readShortSizedString();
                        int id = reader.readVarInt();
                        System.out.println("byte: " + test + " : " + test2);
                        System.out.println("string: " + lvlName + " : " + idName);
                        System.out.println("size: " + lvl + " : " + id);

                        System.out.println("add= " + Enchantment.fromId(id) + " : " + lvl);
                        item.setEnchantment(Enchantment.fromId(id), lvl);
                    }

                    reader.readByte();
                    readItemStackNBT(reader, item);
                }

                break;
            case 0x0A: // TAG_Compound

                String compoundName = reader.readShortSizedString();

                // Display Compound
                if (compoundName.equals("display")) {
                    readItemStackDisplayNBT(reader, item);
                }

                break;
        }
    }

    public static void readItemStackDisplayNBT(PacketReader reader, ItemStack item) {
        byte typeId = reader.readByte();

        switch (typeId) {
            case 0x00: // TAG_End
                // End of the display compound
                readItemStackNBT(reader, item);
                break;
            case 0x08: // TAG_String

                String stringName = reader.readShortSizedString();

                if (stringName.equals("Name")) {
                    item.setDisplayName(reader.readShortSizedString());
                    readItemStackDisplayNBT(reader, item);
                }
                break;
            case 0x09: // TAG_List

                String listName = reader.readShortSizedString();

                if (listName.equals("Lore")) {
                    reader.readByte(); // lore type, should always be 0x08 (TAG_String)

                    int size = reader.readInteger();
                    ArrayList<String> lore = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        lore.add(reader.readShortSizedString());
                        if (lore.size() == size) {
                            item.setLore(lore);
                        }

                        if (i == size - 1) { // Last iteration
                            readItemStackDisplayNBT(reader, item);
                        }
                    }
                }

                break;
        }
    }

}
