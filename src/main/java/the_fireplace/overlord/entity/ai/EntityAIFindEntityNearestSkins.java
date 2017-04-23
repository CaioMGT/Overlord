package the_fireplace.overlord.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import the_fireplace.overlord.entity.EntityArmyMember;
import the_fireplace.overlord.entity.EntityBabySkeleton;
import the_fireplace.overlord.entity.EntityConvertedSkeleton;
import the_fireplace.overlord.entity.EntitySkeletonWarrior;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static the_fireplace.overlord.entity.ai.EntityAITargetSkins.*;

public class EntityAIFindEntityNearestSkins extends EntityAIBase
{
    private static final Logger LOGGER = LogManager.getLogger();
    /** The entity that use this AI */
    private final EntityLiving entityLiving;
    private final Predicate<Entity> predicate;
    /** Used to compare two entities */
    private final EntityAINearestAttackableTarget.Sorter sorter;
    /** The current target */
    private EntityLivingBase entityTarget;

    public EntityAIFindEntityNearestSkins(EntityLiving entityLivingIn)
    {
        this.entityLiving = entityLivingIn;

        if (entityLivingIn instanceof EntityCreature)
        {
            LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinderMob mobs!");
        }

        this.predicate = new Predicate<Entity>()
        {
            @Override
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                if (!(p_apply_1_ instanceof EntityArmyMember))
                {
                    return false;
                }
                else if (p_apply_1_.getIsInvulnerable())
                {
                    return false;
                }
                else
                {
                    double d0 = EntityAIFindEntityNearestSkins.this.maxTargetRange();

                    return !((double) p_apply_1_.getDistanceToEntity(EntityAIFindEntityNearestSkins.this.entityLiving) > d0) && (EntityAITarget.isSuitableTarget(EntityAIFindEntityNearestSkins.this.entityLiving, (EntityLivingBase) p_apply_1_, false, true) && (
                                (p_apply_1_ instanceof EntitySkeletonWarrior && WARRIOR_HAS_SKINSUIT.apply((EntitySkeletonWarrior) p_apply_1_)) ||
                                    (p_apply_1_ instanceof EntityBabySkeleton && BABY_HAS_SKINSUIT.apply((EntityBabySkeleton) p_apply_1_)) ||
                                    (p_apply_1_ instanceof EntityConvertedSkeleton && CONV_HAS_SKINSUIT.apply((EntityConvertedSkeleton) p_apply_1_))));
                }
            }
        };
        this.sorter = new EntityAINearestAttackableTarget.Sorter(entityLivingIn);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        double d0 = this.maxTargetRange();
        List<EntityPlayer> list = this.entityLiving.world.getEntitiesWithinAABB(EntityPlayer.class, this.entityLiving.getEntityBoundingBox().expand(d0, 4.0D, d0), this.predicate);
        Collections.sort(list, this.sorter);

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            this.entityTarget = list.get(0);
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        EntityLivingBase entitylivingbase = this.entityLiving.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }
        else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).capabilities.disableDamage)
        {
            return false;
        }
        else
        {
            Team team = this.entityLiving.getTeam();
            Team team1 = entitylivingbase.getTeam();

            if (team != null && team1 == team)
            {
                return false;
            }
            else
            {
                double d0 = this.maxTargetRange();
                return !(this.entityLiving.getDistanceSqToEntity(entitylivingbase) > d0 * d0) && (!(entitylivingbase instanceof EntityPlayerMP) || !((EntityPlayerMP) entitylivingbase).interactionManager.isCreative());
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.entityLiving.setAttackTarget(this.entityTarget);
        super.startExecuting();
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        this.entityLiving.setAttackTarget(null);
        super.startExecuting();
    }

    /**
     * Return the max target range of the entiity (16 by default)
     */
    protected double maxTargetRange()
    {
        IAttributeInstance iattributeinstance = this.entityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }
}