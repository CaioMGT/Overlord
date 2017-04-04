package the_fireplace.overlord.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import the_fireplace.overlord.Overlord;
import the_fireplace.overlord.config.ConfigValues;
import the_fireplace.overlord.entity.EntityBabySkeleton;
import the_fireplace.overlord.items.ItemOverlordsSeal;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * @author The_Fireplace
 */
public class TileEntityBabySkeletonMaker extends TileEntity implements ISidedInventory, ISkeletonMaker {
    private ItemStack[] inventory;
    public static final int[] clearslots = new int[]{4,5,6,7,8,9};

    public TileEntityBabySkeletonMaker() {
        inventory = new ItemStack[10];
    }

    @Override
    public void spawnSkeleton(){
        UUID owner = null;
        if(!getStackInSlot(0).isEmpty()){
            if(getStackInSlot(0).getTagCompound() != null){
                owner = UUID.fromString(getStackInSlot(0).getTagCompound().getString("Owner"));
                if(getStackInSlot(0).getItem() instanceof ItemOverlordsSeal)
                    if(((ItemOverlordsSeal)getStackInSlot(0).getItem()).isConsumable())
                        getStackInSlot(0).shrink(1);
            }
        }
        EntityBabySkeleton babySkeleton = new EntityBabySkeleton(world, owner);
        babySkeleton.setLocationAndAngles(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, 1, 0);
        babySkeleton.setItemStackToSlot(EntityEquipmentSlot.HEAD, getStackInSlot(7));
        babySkeleton.setItemStackToSlot(EntityEquipmentSlot.CHEST, getStackInSlot(6));
        babySkeleton.setItemStackToSlot(EntityEquipmentSlot.LEGS, getStackInSlot(5));
        babySkeleton.setItemStackToSlot(EntityEquipmentSlot.FEET, getStackInSlot(4));
        babySkeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, getStackInSlot(8));

        world.spawnEntity(babySkeleton);
        if(!getStackInSlot(9).isEmpty())
            babySkeleton.applySkinsuit(getStackInSlot(9));
        for(int i:clearslots){
            setInventorySlotContents(i, ItemStack.EMPTY);
        }
        if(getStackInSlot(2).getItem() == Items.MILK_BUCKET) {
            if (!getStackInSlot(3).isEmpty()) {
                if (getStackInSlot(3).getItem() == Items.BUCKET && getStackInSlot(3).getCount() < getStackInSlot(3).getMaxStackSize())
                    getStackInSlot(3).grow(1);
                else
                    babySkeleton.entityDropItem(new ItemStack(Items.BUCKET), 0.1F);
            } else {
                setInventorySlotContents(3, new ItemStack(Items.BUCKET));
            }
        }else if(getStackInSlot(2).getItem() == Overlord.milk_bottle) {
            if (!getStackInSlot(3).isEmpty()) {
                if (getStackInSlot(3).getItem() == Items.GLASS_BOTTLE && getStackInSlot(3).getCount() < getStackInSlot(3).getMaxStackSize())
                    getStackInSlot(3).grow(1);
                else
                    babySkeleton.entityDropItem(new ItemStack(Items.GLASS_BOTTLE), 0.1F);
            } else {
                setInventorySlotContents(3, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        getStackInSlot(2).shrink(1);
        if(getStackInSlot(2).isEmpty())
            setInventorySlotContents(2, ItemStack.EMPTY);
        if(!getStackInSlot(1).isEmpty()){
            if(getStackInSlot(1).getCount() <= ConfigValues.BONEREQ_BABY)
                setInventorySlotContents(1, ItemStack.EMPTY);
            else
                getStackInSlot(1).shrink(ConfigValues.BONEREQ_BABY);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), getUpdateTag());
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag(){
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    @Nonnull
    public String getName() {
        return Overlord.proxy.translateToLocal("tile.baby_skeleton_maker.name");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("tile.baby_skeleton_maker.name");
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemStack : inventory)
            if(!itemStack.isEmpty())
                return false;
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        if(inventory[index] != null)
            return inventory[index];
        else
            return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        ItemStack is = getStackInSlot(index);
        if (!is.isEmpty()) {
            if (is.getCount() <= count) {
                setInventorySlotContents(index, ItemStack.EMPTY);
            } else {
                is = is.splitStack(count);
                markDirty();
            }
        }
        return is;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        ItemStack is = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return is;
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        inventory[index] = stack;

        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return (index == 0 && stack.getItem() instanceof ItemOverlordsSeal) || (index == 1 && stack.getItem() == Items.BONE) || (index == 2 && (stack.getItem() == Items.MILK_BUCKET || stack.getItem() == Overlord.milk_bottle)) || (index > 3 && index < 8 && stack.getItem().isValidArmor(stack, getSlotEquipmentType(index), null) || (index == 9 && stack.getItem() == Overlord.skinsuit));
    }

    private EntityEquipmentSlot getSlotEquipmentType(int index){
        if(index == 4)
            return EntityEquipmentSlot.FEET;
        if(index == 5)
            return EntityEquipmentSlot.LEGS;
        if(index == 6)
            return EntityEquipmentSlot.CHEST;
        if(index == 7)
            return EntityEquipmentSlot.HEAD;
        if(index == 8)
            return EntityEquipmentSlot.MAINHAND;
        return EntityEquipmentSlot.OFFHAND;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < inventory.length; ++i) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack is = getStackInSlot(i);
            if (!is.isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();

                item.setByte("SlotSkeletonMaker", (byte) i);
                is.writeToNBT(item);

                list.appendTag(item);
            }
        }
        compound.setTag("ItemsSkeletonMaker", list);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList list = (NBTTagList) compound.getTag("ItemsSkeletonMaker");
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound item = (NBTTagCompound) list.get(i);
                int slot = item.getByte("SlotSkeletonMaker");
                if (slot >= 0 && slot < getSizeInventory()) {
                    setInventorySlotContents(slot, new ItemStack(item));
                }
            }
        } else {
            Overlord.logWarn("List was null when reading TileEntityBabySkeletonMaker NBTTagCompound");
        }
    }

    @Override
    @Nonnull
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == EnumFacing.EAST || side == EnumFacing.WEST || side == EnumFacing.NORTH || side == EnumFacing.SOUTH || side == EnumFacing.UP) {
            return new int[]{1, 2, 4, 5, 6, 7, 9};
        }else if (side == EnumFacing.DOWN) {
            return new int[]{3};
        }else{
            throw new IllegalArgumentException("Invalid side: " + side);
        }
    }

    @Override
    public boolean canInsertItem(int index, @Nonnull ItemStack stack, @Nonnull EnumFacing direction) {
        if (!stack.isEmpty()) {
            if (index >= 1 && index < 3 || index >= 4 && index < 8 || index == 9) {
                if(this.isItemValidForSlot(index, stack))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull EnumFacing direction) {
        if (!stack.isEmpty())
            if (index == 3)
                return true;
        return false;
    }

    IItemHandler handlerTop = new SidedInvWrapper(this, EnumFacing.UP);
    IItemHandler handlerBottom = new SidedInvWrapper(this, EnumFacing.DOWN);
    IItemHandler handlerSide = new SidedInvWrapper(this, EnumFacing.WEST);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
    {
        if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            if (facing == EnumFacing.DOWN)
                return (T) handlerBottom;
            else if (facing == EnumFacing.UP)
                return (T) handlerTop;
            else
                return (T) handlerSide;
        return super.getCapability(capability, facing);
    }
}
