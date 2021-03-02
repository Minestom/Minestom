package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.TickUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minestom.server.network.packet.server.play.TitlePacket.Action.*;

public class TitlePacket implements ServerPacket {

    public Action action;

    public String payload;

    public int fadeIn;
    public int stay;
    public int fadeOut;

    /**
     * Constructs a new title packet from an action that can take a string argument.
     *
     * @param action the action
     * @param payload the payload
     * @throws IllegalArgumentException if the action is not {@link Action#SET_TITLE},
     * {@link Action#SET_SUBTITLE} or {@link Action#SET_ACTION_BAR}
     */
    public TitlePacket(@NotNull Action action, @NotNull String payload) {
        Validate.isTrue(action == SET_TITLE || action == SET_SUBTITLE || action == SET_ACTION_BAR, "Invalid action type");
        this.action = action;
        this.payload = payload;
    }

    /**
     * Constructs a new title packet from a clear or reset action.
     *
     * @param action the action
     * @throws IllegalArgumentException if the action is not {@link Action#RESET},
     * or {@link Action#HIDE}
     */
    public TitlePacket(@NotNull Action action) {
        this.action = action;
    }

    /**
     * Constructs a new title packet for {@link Action#SET_TIMES_AND_DISPLAY}.
     *
     * @param fadeIn the fade in time
     * @param stay the stay time
     * @param fadeOut the fade out time
     */
    public TitlePacket(int fadeIn, int stay, int fadeOut) {
        this.action = SET_TIMES_AND_DISPLAY;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case SET_TITLE:
            case SET_SUBTITLE:
            case SET_ACTION_BAR:
                writer.writeSizedString(payload);
                break;
            case SET_TIMES_AND_DISPLAY:
                writer.writeInt(fadeIn);
                writer.writeInt(stay);
                writer.writeInt(fadeOut);
                break;
            case HIDE:
            case RESET:
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TITLE;
    }

    public enum Action {
        SET_TITLE,
        SET_SUBTITLE,
        SET_ACTION_BAR,
        SET_TIMES_AND_DISPLAY,
        HIDE,
        RESET
    }

    /**
     * Creates a collection of title packets from an Adventure title.
     *
     * @param title the title
     * @return the packets
     */
    public static Collection<TitlePacket> of(Title title) {
        List<TitlePacket> packets = new ArrayList<>(4);

        // base packets
        packets.add(new TitlePacket(SET_TITLE, GsonComponentSerializer.gson().serialize(title.title())));
        packets.add(new TitlePacket(SET_SUBTITLE, GsonComponentSerializer.gson().serialize(title.subtitle())));

        // times packet
        Title.Times times = title.times();
        if (times != null) {
            packets.add(new TitlePacket(TickUtils.fromDuration(times.fadeIn()), TickUtils.fromDuration(times.stay()),
                    TickUtils.fromDuration(times.fadeOut())));
        }

        return packets;
    }
}
