package the_fireplace.overlord.entity.ai;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import the_fireplace.overlord.entity.EntityBabySkeleton;
import the_fireplace.overlord.entity.EntityConvertedSkeleton;
import the_fireplace.overlord.entity.EntitySkeletonWarrior;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntityAITargetSkins<T extends EntityLivingBase> extends EntityAITarget
{
    protected final Class<T> targetClass;
    private final int targetChance;
    /** Instance of EntityAINearestAttackableTargetSorter. */
    protected final EntityAITargetSkins.Sorter theNearestAttackableTargetSorter;
    protected final Predicate <? super T > targetEntitySelector;
    protected T targetEntity;

    public static final Predicate<EntitySkeletonWarrior> WARRIOR_HAS_SKINSUIT = new Predicate<EntitySkeletonWarrior>()
    {
        @Override
        public boolean apply(@Nullable EntitySkeletonWarrior p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.hasSkinsuit();
        }
    };

    public static final Predicate<EntityBabySkeleton> BABY_HAS_SKINSUIT = new Predicate<EntityBabySkeleton>()
    {
        @Override
        public boolean apply(@Nullable EntityBabySkeleton p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.hasSkinsuit();
        }
    };

    public static final Predicate<EntityConvertedSkeleton> CONV_HAS_SKINSUIT = new Predicate<EntityConvertedSkeleton>()
    {
        @Override
        public boolean apply(@Nullable EntityConvertedSkeleton p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.hasSkinsuit();
        }
    };

    public EntityAITargetSkins(EntityCreature creature, Class<T> classTarget, boolean checkSight)
    {
        this(creature, classTarget, checkSight, false);
    }

    public EntityAITargetSkins(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby)
    {
        this(creature, classTarget, 10, checkSight, onlyNearby);
    }

    public EntityAITargetSkins(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby)
    {
        super(creature, checkSight, onlyNearby);
        this.targetClass = classTarget;
        this.targetChance = chance;
        this.theNearestAttackableTargetSorter = new EntityAITargetSkins.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>()
        {
            @Override
            public boolean apply(@Nullable T p_apply_1_)
            {
                return p_apply_1_ != null && EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) && EntityAITargetSkins.this.isSuitableTarget(p_apply_1_, false) && (
                                (p_apply_1_ instanceof EntitySkeletonWarrior && WARRIOR_HAS_SKINSUIT.apply((EntitySkeletonWarrior) p_apply_1_)) ||
                                (p_apply_1_ instanceof EntityBabySkeleton && BABY_HAS_SKINSUIT.apply((EntityBabySkeleton) p_apply_1_)) ||
                                (p_apply_1_ instanceof EntityConvertedSkeleton && CONV_HAS_SKINSUIT.apply((EntityConvertedSkeleton) p_apply_1_)));
            }
        };
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0)
        {
            return false;
        }
        else if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class)
        {
            List<T> list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);

            if (list.isEmpty())
            {
                return false;
            }
            else
            {
                Collections.sort(list, this.theNearestAttackableTargetSorter);
                this.targetEntity = list.get(0);
                return true;
            }
        }
        else
        {
            this.targetEntity = (T)this.taskOwner.world.getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double)this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(), new Function<EntityPlayer, Double>()
            {
                @Override
                @Nullable
                public Double apply(@Nullable EntityPlayer p_apply_1_)
                {
                    ItemStack itemstack = p_apply_1_.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                    if (itemstack.getItem() == Items.SKULL)
                    {
                        int i = itemstack.getItemDamage();
                        boolean flag = EntityAITargetSkins.this.taskOwner instanceof EntitySkeleton && i == 0;
                        boolean flag1 = EntityAITargetSkins.this.taskOwner instanceof EntityZombie && i == 2;
                        boolean flag2 = EntityAITargetSkins.this.taskOwner instanceof EntityCreeper && i == 4;

                        if (flag || flag1 || flag2)
                        {
                            return Double.valueOf(0.5D);
                        }
                    }

                    return Double.valueOf(1.0D);
                }
            }, (Predicate<EntityPlayer>)this.targetEntitySelector);
            return this.targetEntity != null;
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance)
    {
        return this.taskOwner.getEntityBoundingBox().expand(targetDistance, 4.0D, targetDistance);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    public static class Sorter implements Comparator<Entity>
        {
            private final Entity theEntity;

            public Sorter(Entity theEntityIn)
            {
                this.theEntity = theEntityIn;
            }

            @Override
            public int compare(Entity p_compare_1_, Entity p_compare_2_)
            {
                double d0 = this.theEntity.getDistanceSqToEntity(p_compare_1_);
                double d1 = this.theEntity.getDistanceSqToEntity(p_compare_2_);
                return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
            }
        }
}