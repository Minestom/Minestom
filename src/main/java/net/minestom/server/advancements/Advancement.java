package net.minestom.server.advancements;

import io.netty.buffer.ByteBuf;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;

public class Advancement {

    protected AdvancementTab tab;

    private ColoredText title;
    private ColoredText description;

    private ItemStack icon;

    private FrameType frameType;

    private String background; // Only on root
    private boolean toast;
    private boolean hidden;

    private float x, y;

    private String identifier;
    private Advancement parent;

    public Advancement(ColoredText title, ColoredText description,
                       ItemStack icon, FrameType frameType,
                       float x, float y) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.frameType = frameType;
        this.x = x;
        this.y = y;
    }

    public Advancement(ColoredText title, ColoredText description,
                       Material icon, FrameType frameType,
                       float x, float y) {
        this(title, description, new ItemStack(icon, (byte) 1), frameType, x, y);
    }

    public AdvancementTab getTab() {
        return tab;
    }

    public void setTab(AdvancementTab tab) {
        this.tab = tab;
    }

    public ColoredText getTitle() {
        return title;
    }

    public void setTitle(ColoredText title) {
        this.title = title;
        update();
    }

    public ColoredText getDescription() {
        return description;
    }

    public void setDescription(ColoredText description) {
        this.description = description;
        update();
    }

    public boolean hasToast() {
        return toast;
    }

    public Advancement showToast(boolean toast) {
        this.toast = toast;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }

    public Advancement setHidden(boolean hidden) {
        this.hidden = hidden;
        update();
        return this;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
        update();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        update();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        update();
    }

    protected void setBackground(String background) {
        this.background = background;
    }

    protected String getIdentifier() {
        return identifier;
    }

    protected void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    protected Advancement getParent() {
        return parent;
    }

    protected void setParent(Advancement parent) {
        this.parent = parent;
    }

    protected AdvancementsPacket.DisplayData toDisplayData() {
        AdvancementsPacket.DisplayData displayData = new AdvancementsPacket.DisplayData();
        displayData.x = x;
        displayData.y = y;
        displayData.title = title;
        displayData.description = description;
        displayData.icon = icon;
        displayData.frameType = frameType;
        displayData.flags = getFlags();
        if (background != null) {
            displayData.backgroundTexture = background;
        }
        return displayData;
    }

    /**
     * Update this advancement value when a field is modified
     */
    protected void update() {
        if (tab != null) {
            tab.createBuffer = PacketUtils.writePacket(tab.createPacket());

            final ByteBuf createBuffer = tab.createBuffer;
            final ByteBuf removeBuffer = tab.removeBuffer;
            tab.getViewers().forEach(player -> {
                final PlayerConnection playerConnection = player.getPlayerConnection();
                playerConnection.sendPacket(removeBuffer, true);
                playerConnection.sendPacket(createBuffer, true);
            });
        }
    }

    private int getFlags() {
        byte result = 0;

        if (background != null) {
            result |= 0x1;
        }

        if (hasToast()) {
            result |= 0x2;
        }

        if (isHidden()) {
            result |= 0x4;
        }

        return result;
    }

}
