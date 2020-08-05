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

    /**
     * Get the advancement tab linked to this advancement
     *
     * @return the {@link AdvancementTab} linked to this advancement
     */
    public AdvancementTab getTab() {
        return tab;
    }

    protected void setTab(AdvancementTab tab) {
        this.tab = tab;
    }

    /**
     * Get the title of the advancement
     *
     * @return the advancement title
     */
    public ColoredText getTitle() {
        return title;
    }

    /**
     * Change the advancement title
     *
     * @param title the new title
     */
    public void setTitle(ColoredText title) {
        this.title = title;
        update();
    }

    /**
     * Get the description of the advancement
     *
     * @return the description title
     */
    public ColoredText getDescription() {
        return description;
    }

    /**
     * Change the description title
     *
     * @param description the new description
     */
    public void setDescription(ColoredText description) {
        this.description = description;
        update();
    }

    /**
     * Get the advancement icon
     *
     * @return the advancement icon
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Change the advancement icon
     *
     * @param icon the new advancement icon
     */
    public void setIcon(ItemStack icon) {
        this.icon = icon;
        update();
    }

    /**
     * Get if this advancement has a toast
     *
     * @return true if the advancement has a toast
     */
    public boolean hasToast() {
        return toast;
    }

    /**
     * Make this argument a toast
     *
     * @param toast true to make this advancement a toast
     * @return this advancement
     */
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

    /**
     * Get the advancement frame type
     *
     * @return this advancement frame type
     */
    public FrameType getFrameType() {
        return frameType;
    }

    /**
     * Change the advancement frame type
     *
     * @param frameType the new frame type
     */
    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
        update();
    }

    /**
     * Get the X position of this advancement
     *
     * @return this advancement X
     */
    public float getX() {
        return x;
    }

    /**
     * Change this advancement X coordinate
     *
     * @param x the new X coordinate
     */
    public void setX(float x) {
        this.x = x;
        update();
    }

    /**
     * Get the Y position of this advancement
     *
     * @return this advancement Y
     */
    public float getY() {
        return y;
    }

    /**
     * Change this advancement Y coordinate
     *
     * @param y the new Y coordinate
     */
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
     * Get the packet used to add this advancement to the already existing tab
     *
     * @return the packet to add this advancement
     */
    protected AdvancementsPacket getUpdatePacket() {
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;

        AdvancementsPacket.AdvancementMapping mapping = new AdvancementsPacket.AdvancementMapping();
        {
            AdvancementsPacket.Advancement adv = new AdvancementsPacket.Advancement();
            mapping.key = getIdentifier();
            mapping.value = adv;

            final Advancement parent = getParent();
            if (parent != null) {
                final String parentIdentifier = parent.getIdentifier();
                adv.parentIdentifier = parentIdentifier;
            }

            adv.displayData = toDisplayData();
            adv.criterions = new String[]{};
            adv.requirements = new AdvancementsPacket.Requirement[]{};
        }

        advancementsPacket.identifiersToRemove = new String[]{};
        advancementsPacket.advancementMappings = new AdvancementsPacket.AdvancementMapping[]{mapping};
        advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{};

        return advancementsPacket;
    }

    /**
     * Send update to all tab viewers if one of the advancement value changes
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
