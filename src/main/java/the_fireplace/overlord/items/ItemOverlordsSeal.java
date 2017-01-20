package the_fireplace.overlord.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.overlord.Overlord;

import java.util.List;

import static the_fireplace.overlord.Overlord.proxy;

/**
 * @author The_Fireplace
 */
public class ItemOverlordsSeal extends Item {

    private boolean canOpenGui;
    private boolean consumable;

    public ItemOverlordsSeal(){
        this(true, false);
    }

    public ItemOverlordsSeal(boolean canOpenGui, boolean consumedOnUse){
        this.canOpenGui = canOpenGui;
        this.consumable = consumedOnUse;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());
        if (!stack.getTagCompound().hasKey("Owner")) {
            stack.getTagCompound().setString("Owner", playerIn.getUniqueID().toString());
            stack.getTagCompound().setString("OwnerName", playerIn.getDisplayNameString());
            return new ActionResult(EnumActionResult.SUCCESS, stack);
        }else if(canOpenGui) {
            if(stack.getTagCompound().getString("Owner").equals(playerIn.getUniqueID().toString())){
                FMLNetworkHandler.openGui(playerIn, Overlord.instance, -1, worldIn, (int)playerIn.posX, (int)playerIn.posY, (int)playerIn.posZ);
                if(consumable)
                    stack.stackSize--;
                return new ActionResult(EnumActionResult.SUCCESS, stack);
            }else{
                return new ActionResult(EnumActionResult.FAIL, stack);
            }
        }else{
            return new ActionResult(EnumActionResult.PASS, stack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("Owner")) {
            tooltip.add(proxy.translateToLocal(getUnlocalizedName()+".tooltip", stack.getTagCompound().getString("OwnerName"), stack.stackSize > 1 ? "s" : ""));
        }else{
            tooltip.add(proxy.translateToLocal(getUnlocalizedName()+".tooltip.default", stack.stackSize > 1 ? "ese" : "is", stack.stackSize > 1 ? "s" : ""));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey("Owner");
    }

    public boolean isConsumable(){
        return consumable;
    }
}
