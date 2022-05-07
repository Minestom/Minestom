package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents an advancement located in an {@link AdvancementTab}.
 * <p>
 * All fields are dynamic, changing one will update the advancement in the specific {@link AdvancementTab}.
 */
public class Advancement {

    protected AdvancementTab tab;

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

    private final Map<String, Criterion> criteriaMap = new HashMap<>();
    private final List<List<String>> requirements = new ArrayList<>();

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
     * Gets if the criterion is achieved.
     *
     * @param criterionIdentifier an identifier of added criterion
     * @return true if the advancement is achieved
     */
    public boolean isAchieved(String criterionIdentifier) {
        return criteriaMap.get(criterionIdentifier) != null && criteriaMap.get(criterionIdentifier).isAchieved();
    }

    /**
     * Makes the criterion achieved. Before this
     * you need to create a criterion from Advancement#addCriterion
     *
     * @param criterionIdentifier an identifier of added criterion
     * @param achieved true to make it achieved
     * @return this advancement
     */
    public Advancement setAchieved(String criterionIdentifier, boolean achieved) {
        Criterion criterion = criteriaMap.get(criterionIdentifier);
        if (criterion != null) {
            criterion.setAchieved(achieved);
        }
        return this;
    }

    public void addCriterion(String criterionIdentifier) {
        this.addCriterion(criterionIdentifier, false);
    }

    /**
     * Adds new criterion with its own requirement to the advancement.
     * You can change its state with Advancement#setAchieved, or you
     * can get criterion and call Criterion#setAchieved
     *
     * @param criterionIdentifier A criterion identifier to add
     * @param achieved should criterion be achieved
     */
    public void addCriterion(String criterionIdentifier, boolean achieved) {
        this.criteriaMap.put(criterionIdentifier, new Criterion(this, criterionIdentifier, achieved));
        this.requirements.add(new ArrayList<>(List.of(criterionIdentifier)));
        update();
    }

    /**
     * Adds criteria to the same requirement.
     *
     * @param criteriaIdentifiers criteria identifiers
     */
    public void addCriteriaToSameRequirement(String... criteriaIdentifiers) {
        for (String criterionIdentifier : criteriaIdentifiers) {
            this.criteriaMap.put(criterionIdentifier, new Criterion(this, criterionIdentifier, false));
        }
        this.requirements.add(new ArrayList<>(List.of(criteriaIdentifiers)));
        update();
    }

    /**
     * Removes criterion from advancement (from criteria
     * map and requirements)
     *
     * @param criterionIdentifier criterion to remove
     */
    public void removeCriterion(String criterionIdentifier) {
        this.criteriaMap.remove(criterionIdentifier);

        // Remove from requirements
        var iterator = this.requirements.listIterator();
        while (iterator.hasNext()) {
            List<String> requirement = iterator.next();
            requirement.removeIf(id -> id.equals(criterionIdentifier));
            if (requirement.size() == 0) {
                iterator.remove();
            }
        }
        update();
    }

    @Nullable
    public Criterion getCriterion(String criterionIdentifier) {
        return this.criteriaMap.get(criterionIdentifier);
    }

    /**
     * Gets all criteria
     *
     * @return returns criteria list
     */
    public List<Criterion> getCriteriaList() {
        return List.copyOf(this.criteriaMap.values());
    }

    public List<List<String>> getRequirements() {
        return List.copyOf(this.requirements);
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
     * @return the advancement identifier, null when advancement is not registered
     */
    @Nullable
    public String getIdentifier() {
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
    public Advancement getParent() {
        return parent;
    }

    /**
     * Changes the advancement parent
     * * <p>
     * WARNING: unsafe, only used by {@link AdvancementTab} to initialize the advancement.
     *
     * @param parent Parent to set
     */
    protected void setParent(Advancement parent) {
        this.parent = parent;
    }

    protected @NotNull List<AdvancementsPacket.Criteria> toCriteriaPacketList() {
        List<AdvancementsPacket.Criteria> criteriaPacketList = new ArrayList<>();
        for (Criterion criteria : criteriaMap.values()) {
            criteriaPacketList.add(criteria.toCriteriaPacket());
        }
        return criteriaPacketList;
    }

    protected @NotNull List<AdvancementsPacket.Requirement> toRequirementList() {
        List<AdvancementsPacket.Requirement> requirementList = new ArrayList<>();
        for (List<String> identifierList : this.requirements) {
            requirementList.add(new AdvancementsPacket.Requirement(identifierList));
        }
        return requirementList;
    }

    protected @NotNull AdvancementsPacket.ProgressMapping toProgressMapping() {
        final var advancementProgress = new AdvancementsPacket.AdvancementProgress(toCriteriaPacketList());
        return new AdvancementsPacket.ProgressMapping(identifier, advancementProgress);
    }

    protected @NotNull AdvancementsPacket.DisplayData toDisplayData() {
        return new AdvancementsPacket.DisplayData(title, description, icon,
                frameType, getFlags(), background, x, y);
    }

    /**
     * Converts this advancement to an {@link AdvancementsPacket.AdvancementMapping}.
     *
     * @return the mapping of this advancement
     */
    protected @NotNull AdvancementsPacket.AdvancementMapping toMapping() {
        final Advancement parent = getParent();
        final String parentIdentifier = parent != null ? parent.getIdentifier() : null;
        AdvancementsPacket.Advancement adv = new AdvancementsPacket.Advancement(parentIdentifier, toDisplayData(),
                criteriaMap.keySet(),
                toRequirementList());
        return new AdvancementsPacket.AdvancementMapping(getIdentifier(), adv);
    }

    /**
     * Gets the packet used to add this advancement to the already existing tab.
     *
     * @return the packet to add this advancement
     */
    protected AdvancementsPacket getUpdatePacket() {
        return new AdvancementsPacket(false, List.of(toMapping()),
                List.of(), List.of(toProgressMapping()));
    }

    /**
     * Sends update to all tab viewers if one of the advancement value changes.
     */
    protected void update() {
        if (tab != null) {
            final Set<Player> viewers = tab.getViewers();
            AdvancementsPacket createPacket = tab.createPacket();

            PacketUtils.sendGroupedPacket(viewers, tab.removePacket);
            PacketUtils.sendGroupedPacket(viewers, createPacket);
        }
    }

    private int getFlags() {
        byte result = 0;
        if (background != null) result |= 0x1;
        if (hasToast()) result |= 0x2;
        if (isHidden()) result |= 0x4;
        return result;
    }
}
