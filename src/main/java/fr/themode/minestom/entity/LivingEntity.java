package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;

// TODO attributes https://wiki.vg/Protocol#Entity_Properties
public abstract class LivingEntity extends Entity {

    protected boolean onGround;

    private boolean isHandActive;
    private boolean activeHand;
    private boolean riptideSpinAttack;

    public LivingEntity(int entityType) {
        super(entityType);
    }

    @Override
    public Buffer getMetadataBuffer() {
        Buffer buffer = super.getMetadataBuffer();
        buffer.putByte((byte) 7);
        buffer.putByte(METADATA_BYTE);
        byte activeHandValue = 0;
        if (isHandActive) {
            activeHandValue += 1;
            if (activeHand)
                activeHandValue += 2;
            if (riptideSpinAttack)
                activeHandValue += 4;
        }
        buffer.putByte(activeHandValue);
        return buffer;
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        this.isHandActive = isHandActive;
        this.activeHand = offHand;
        this.riptideSpinAttack = riptideSpinAttack;
    }
}
