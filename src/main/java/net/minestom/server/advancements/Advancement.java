package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Set;

/**
 * Represents an advancement located in an {@link AdvancementTab}.
 * <p>
 * All fields are dynamic, changing one will update the advancement in the specific {@link AdvancementTab}.
 */
public class Advancement {

    protected AdvancementTab tab;

    private boolean achieved;

    private Component title;
    private Component description;

    private ItemStack icon;

    private FrameType frameType;

    private String background; // Only on root
    private boolean toast;
    private boolean hidden;

    private float x, y;

    private String identifier;
    private Advancement parent;

    // Packet
    private AdvancementsPacket.Criteria criteria;

    public Advancement(@NotNull Component title, Component description,
                       @NotNull ItemStack icon, @NotNull FrameType frameType,
                       float x, float y) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.frameType = frameType;
        this.x = x;
        this.y = y;
    }

    public Advancement(@NotNull Component title, @NotNull Component description,
                       @NotNull Material icon, @NotNull FrameType frameType,
                       float x, float y) {
        this(title, description, ItemStack.of(icon), frameType, x, y);
    }

    /**
     * Gets if the advancement is achieved.
     *
     * @return true if the advancement is achieved
     */
    public boolean isAchieved() {
        return achieved;
    }

    /**
     * Makes the advancement achieved.
     *
     * @param achieved true to make it achieved
     * @return this advancement
     */
    public Advancement setAchieved(boolean achieved) {
        this.achieved = achieved;
        update();
        return this;
    }

    /**
     * Gets the advancement tab linked to this advancement.
     *
     * @return the {@link AdvancementTab} linked to this advancement, null if not linked to anything yet
     */
    public @Nullable AdvancementTab getTab() {
        return tab;
    }

    protected void setTab(@NotNull AdvancementTab tab) {
        this.tab = tab;
    }

    /**
     * Gets the title of the advancement.
     *
     * @return the title
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Changes the advancement title.
     *
     * @param title the new title
     */
    public void setTitle(@NotNull Component title) {
        this.title = title;
        update();
    }

    /**
     * Gets the description of the advancement.
     *
     * @return the description title
     */
    public @NotNull Component getDescription() {
        return description;
    }

    /**
     * Changes the description title.
     *
     * @param description the new description
     */
    public void setDescription(@NotNull Component description) {
        this.description = description;
        update();
    }

    /**
     * Gets the advancement icon.
     *
     * @return the advancement icon
     */
    public @NotNull ItemStack getIcon() {
        return icon;
    }

    /**
     * Changes the advancement icon.
     *
     * @param icon the new advancement icon
     */
    public void setIcon(@NotNull ItemStack icon) {
        this.icon = icon;
        update();
    }

    /**
     * Gets if this advancement has a toast.
     *
     * @return true if the advancement has a toast
     */
    public boolean hasToast() {
        return toast;
    }

    /**
     * Makes this argument a toast.
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
     * Gets the advancement frame type.
     *
     * @return this advancement frame type
     */
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    /**
     * Changes the advancement frame type.
     *
     * @param frameType the new frame type
     */
    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
        update();
    }

    /**
     * Gets the X position of this advancement.
     *
     * @return this advancement X
     */
    public float getX() {
        return x;
    }

    /**
     * Changes this advancement X coordinate.
     *
     * @param x the new X coordinate
     */
    public void setX(float x) {
        this.x = x;
        update();
    }

    /**
     * Gets the Y position of this advancement.
     *
     * @return this advancement Y
     */
    public float getY() {
        return y;
    }

    /**
     * Changes this advancement Y coordinate.
     *
     * @param y the new Y coordinate
     */
    public void setY(float y) {
        this.y = y;
        update();
    }

    /**
     * Sets the background.
     * <p>
     * Only available for {@link AdvancementRoot}.
     *
     * @param background the new background
     */
    protected void setBackground(String background) {
        this.background = background;
    }

    /**
     * Gets the identifier of this advancement, used to register the advancement, use it as a parent and to retrieve it later
     * in the {@link AdvancementTab}.
     *
     * @return the advancement identifier
     */
    protected String getIdentifier() {
        return identifier;
    }

    /**
     * Changes the advancement identifier.
     * <p>
     * WARNING: unsafe, only used by {@link AdvancementTab} to initialize the advancement.
     *
     * @param identifier the new advancement identifier
     */
    protected void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the advancement parent.
     *
     * @return the advancement parent, null for {@link AdvancementRoot}
     */
    @Nullable
    protected Advancement getParent() {
        return parent;
    }

    protected void setParent(Advancement parent) {
        this.parent = parent;
    }

    @NotNull
    protected AdvancementsPacket.ProgressMapping toProgressMapping() {
        AdvancementsPacket.ProgressMapping progressMapping = new AdvancementsPacket.ProgressMapping();
        {
            AdvancementsPacket.AdvancementProgress advancementProgress = new AdvancementsPacket.AdvancementProgress();
            advancementProgress.criteria = new AdvancementsPacket.Criteria[]{criteria};

            progressMapping.key = identifier;
            progressMapping.value = advancementProgress;
        }
        return progressMapping;
    }

    @NotNull
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
     * Converts this advancement to an {@link AdvancementsPacket.AdvancementMapping}.
     *
     * @return the mapping of this advancement
     */
    @NotNull
    protected AdvancementsPacket.AdvancementMapping toMapping() {
        AdvancementsPacket.AdvancementMapping mapping = new AdvancementsPacket.AdvancementMapping();
        {
            AdvancementsPacket.Advancement adv = new AdvancementsPacket.Advancement();
            mapping.key = getIdentifier();
            mapping.value = adv;

            final Advancement parent = getParent();
            if (parent != null) {
                adv.parentIdentifier = parent.getIdentifier();
            }

            adv.displayData = toDisplayData();
            adv.criterions = new String[]{criteria.criterionIdentifier};

            AdvancementsPacket.Requirement requirement = new AdvancementsPacket.Requirement();
            {
                requirement.requirements = new String[]{criteria.criterionIdentifier};
            }
            adv.requirements = new AdvancementsPacket.Requirement[]{requirement};

        }

        return mapping;
    }

    /**
     * Gets the packet used to add this advancement to the already existing tab.
     *
     * @return the packet to add this advancement
     */
    protected AdvancementsPacket getUpdatePacket() {
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;

        final AdvancementsPacket.AdvancementMapping mapping = toMapping();

        advancementsPacket.identifiersToRemove = new String[]{};
        advancementsPacket.advancementMappings = new AdvancementsPacket.AdvancementMapping[]{mapping};
        advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{toProgressMapping()};

        return advancementsPacket;
    }

    /**
     * Sends update to all tab viewers if one of the advancement value changes.
     */
    protected void update() {
        updateCriteria();

        if (tab != null) {
            final Set<Player> viewers = tab.getViewers();
            AdvancementsPacket createPacket = tab.createPacket();

            PacketUtils.sendGroupedPacket(viewers, tab.removePacket);
            PacketUtils.sendGroupedPacket(viewers, createPacket);
        }
    }

    protected void updateCriteria() {
        this.criteria = new AdvancementsPacket.Criteria();
        {
            AdvancementsPacket.CriterionProgress progress = new AdvancementsPacket.CriterionProgress();
            progress.achieved = achieved;
            if (achieved) {
                progress.dateOfAchieving = new Date(System.currentTimeMillis()).getTime();
            }
            this.criteria.criterionProgress = progress;
            this.criteria.criterionIdentifier = identifier;
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
