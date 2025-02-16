package dev.the_fireplace.overlord.entity.ai.goal.equipment;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.overlord.entity.ArmyEntity;
import dev.the_fireplace.overlord.entity.ai.goal.AIEquipmentHelper;

public class SwitchToRangedWhenFarGoal extends SwapEquipmentGoal
{
    protected final byte switchDistance;
    protected final AIEquipmentHelper equipmentHelper;

    protected byte postSwapCooldownTicks;

    public SwitchToRangedWhenFarGoal(ArmyEntity armyEntity, byte switchDistance) {
        super(armyEntity);
        this.switchDistance = switchDistance;
        this.equipmentHelper = DIContainer.get().getInstance(AIEquipmentHelper.class);
    }

    @Override
    public boolean canStart() {
        return super.canStart()
            && isInRangedDistance()
            && !equipmentHelper.isUsingRanged(armyEntity)
            && equipmentHelper.hasUsableRangedWeapon(armyEntity);
    }

    private boolean isInRangedDistance() {
        return armyEntity.getTarget() != null
            && armyEntity.squaredDistanceTo(armyEntity.getTarget()) > (switchDistance * switchDistance);
    }

    @Override
    public void start() {
        super.start();
        this.postSwapCooldownTicks = 0;
    }

    @Override
    public boolean shouldContinue() {
        return this.postSwapCooldownTicks > 0
            || !equipmentHelper.isUsingRanged(armyEntity)
            || !equipmentHelper.hasAmmoEquipped(armyEntity);
    }

    @Override
    public void tick() {
        if (this.postSwapCooldownTicks > 0) {
            this.postSwapCooldownTicks--;
            return;
        }

        if (!equipmentHelper.isUsingRanged(armyEntity)) {
            equipmentHelper.equipUsableRangedWeapon(armyEntity);
            this.postSwapCooldownTicks = this.armyEntity.getEquipmentSwapTicks();
            return;
        }
        if (!equipmentHelper.hasAmmoEquipped(armyEntity)) {
            equipmentHelper.equipUsableAmmo(armyEntity);
            this.postSwapCooldownTicks = this.armyEntity.getEquipmentSwapTicks();
        }
    }
}
