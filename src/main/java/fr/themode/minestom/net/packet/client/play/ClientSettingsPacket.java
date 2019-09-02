package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientSettingsPacket extends ClientPlayPacket {

    public String locale;
    public byte viewDistance;
    public Player.ChatMode chatMode;
    public boolean chatColors;
    public byte displayedSkinParts;
    public Player.MainHand mainHand;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readSizedString(s -> locale = s);
        reader.readByte(value -> viewDistance = value);
        reader.readVarInt(value -> chatMode = Player.ChatMode.values()[value]);
        reader.readBoolean(value -> chatColors = value);
        reader.readByte(value -> displayedSkinParts = value);
        reader.readVarInt(value -> {
            mainHand = Player.MainHand.values()[value];
            callback.run();
        });
    }
}
