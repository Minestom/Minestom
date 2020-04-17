package fr.themode.minestom.utils;

import club.thectm.minecraft.text.LegacyText;
import club.thectm.minecraft.text.TextObject;
import com.google.gson.JsonParser;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketReader;

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
                    boolean end = false;
                    while (!end) {
                        reader.readByte(); // Should be a compound (0x0A)
                        int size = reader.readInteger();

                        byte test = reader.readByte();
                        String lvlName = reader.readShortSizedString();
                        short lvl = reader.readShort();
                        byte test2 = reader.readByte();
                        String idName = reader.readShortSizedString();
                        short id = reader.readShort();
                        System.out.println("size: " + lvl + " : " + id);

                        end = true;
                    }
                    // TODO
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
                    String jsonDisplayName = reader.readShortSizedString();
                    TextObject textObject = TextObject.fromJson(new JsonParser().parse(jsonDisplayName).getAsJsonObject());
                    String displayName = LegacyText.toLegacy(textObject);
                    item.setDisplayName(displayName);
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
                        String string = reader.readShortSizedString();

                        TextObject textObject = TextObject.fromJson(new JsonParser().parse(string).getAsJsonObject());
                        String line = LegacyText.toLegacy(textObject);
                        lore.add(line);
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
