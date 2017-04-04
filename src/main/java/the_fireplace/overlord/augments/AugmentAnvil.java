package the_fireplace.overlord.augments;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import the_fireplace.overlord.entity.EntityArmyMember;
import the_fireplace.overlord.tools.Augment;

import javax.annotation.Nonnull;

/**
 * @author The_Fireplace
 */
public class AugmentAnvil extends Augment {
    @Override
    public void onStrike(@Nonnull EntityArmyMember attacker, @Nonnull Entity entityAttacked) {

    }

    @Nonnull
    @Override
    public String augmentId() {
        return "iron_anvil";
    }

    @Override
    public void onEntityTick(@Nonnull EntityArmyMember entity) {
        if(entity.motionY < 0){
            entity.motionY *= 1.5;
        }
        if(entity.fallDistance > 2){
            if(!entity.world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().offset(new BlockPos(0, -2, 0))).isEmpty())
                for(EntityLivingBase fallenon:entity.world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().offset(new BlockPos(0, -2, 0)).expandXyz(1))){
                    fallenon.attackEntityFrom(DamageSource.ANVIL, (float)Math.min(MathHelper.floor((float)MathHelper.ceil(entity.fallDistance - 1.0F) * 2.0F)/2, 40));
                }
        }
        if(entity.getActivePotionEffect(MobEffects.RESISTANCE) == null)
            entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120));
    }
}
