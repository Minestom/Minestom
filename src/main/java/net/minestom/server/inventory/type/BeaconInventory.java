package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.potion.PotionType;

public class BeaconInventory extends Inventory {

    private short powerLevel;
    private PotionType firstPotionEffect;
    private PotionType secondPotionEffect;

    public BeaconInventory(String title) {
        super(InventoryType.BEACON, title);
    }

    /**
     * Get the beacon power level
     *
     * @return the power level
     */
    public short getPowerLevel() {
        return powerLevel;
    }

    /**
     * Change the beacon power level
     *
     * @param powerLevel the new beacon power level
     */
    public void setPowerLevel(short powerLevel) {
        this.powerLevel = powerLevel;
        sendProperty(InventoryProperty.BEACON_POWER_LEVEL, powerLevel);
    }

    /**
     * Get the first potion effect
     *
     * @return the first potion effect, can be null
     */
    public PotionType getFirstPotionEffect() {
        return firstPotionEffect;
    }

    /**
     * Change the first potion effect
     *
     * @param firstPotionEffect the new first potion effect, can be null
     */
    public void setFirstPotionEffect(PotionType firstPotionEffect) {
        this.firstPotionEffect = firstPotionEffect;
        sendProperty(InventoryProperty.BEACON_FIRST_POTION, (short) firstPotionEffect.getId());
    }

    /**
     * Get the second potion effect
     *
     * @return the second potion effect, can be null
     */
    public PotionType getSecondPotionEffect() {
        return secondPotionEffect;
    }

    /**
     * Change the second potion effect
     *
     * @param secondPotionEffect the new second potion effect, can be null
     */
    public void setSecondPotionEffect(PotionType secondPotionEffect) {
        this.secondPotionEffect = secondPotionEffect;
        sendProperty(InventoryProperty.BEACON_SECOND_POTION, (short) secondPotionEffect.getId());
    }

}
