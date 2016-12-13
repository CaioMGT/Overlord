package the_fireplace.overlord.augments;

import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import the_fireplace.overlord.entity.EntityArmyMember;
import the_fireplace.overlord.tools.Augment;

import javax.annotation.Nonnull;

/**
 * @author The_Fireplace
 */
public class AugmentIron extends Augment {
    @Override
    @Nonnull
    public String augmentId() {
        return "iron";
    }

    @Override
    public void onEntityTick(EntityArmyMember entity) {
        if(entity.getActivePotionEffect(MobEffects.RESISTANCE) == null)
            entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120));
    }

    @Override
    public void onStrike(EntityArmyMember attacker, Entity entityAttacked) {

    }
}
