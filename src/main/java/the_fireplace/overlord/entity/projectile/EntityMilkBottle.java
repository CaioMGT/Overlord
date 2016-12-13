package the_fireplace.overlord.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import the_fireplace.overlord.entity.EntityBabySkeleton;
import the_fireplace.overlord.entity.EntitySkeletonWarrior;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author The_Fireplace
 */
public class EntityMilkBottle extends EntityThrowable {
    public EntityMilkBottle(World worldIn) {
        super(worldIn);
    }

    public EntityMilkBottle(World worldIn, EntityLivingBase entity) {
        super(worldIn, entity);
    }

    public EntityMilkBottle(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected void onImpact(@Nonnull RayTraceResult mop) {
        if (!this.worldObj.isRemote) {
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
            List<EntityLivingBase> list = this.worldObj.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

            if (!list.isEmpty())
            {
                for (EntityLivingBase entitylivingbase : list)
                {
                    if (entitylivingbase instanceof EntitySkeletonWarrior || entitylivingbase instanceof EntityBabySkeleton || entitylivingbase instanceof EntitySkeleton)
                    {
                        double d0 = this.getDistanceSqToEntity(entitylivingbase);

                        if (d0 < 16.0D)
                        {
                            entitylivingbase.heal(1);
                        }
                    }
                }
            }
            this.setDead();
        }
    }
}