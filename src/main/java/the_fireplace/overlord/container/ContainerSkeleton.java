package the_fireplace.overlord.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.overlord.entity.EntitySkeletonWarrior;
import the_fireplace.overlord.network.PacketDispatcher;
import the_fireplace.overlord.network.packets.RequestAugmentMessage;
import the_fireplace.overlord.registry.AugmentRegistry;

import javax.annotation.Nonnull;

/**
 * @author The_Fireplace
 */
public class ContainerSkeleton extends Container {
    private EntitySkeletonWarrior entity;
    private static final EntityEquipmentSlot[] EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

    public ContainerSkeleton(InventoryPlayer invPlayer, EntitySkeletonWarrior entity){
        this.entity=entity;
        InventoryBasic entityInv = entity.inventory;
        InventoryBasic armorInv = entity.equipInventory;
        for (int x = 0; x < 9; x++) {
            this.addSlotToContainer(new Slot(invPlayer, x, 8 + x * 18, 142));//player inventory IDs 0 to 8
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new Slot(invPlayer, 9 + x + y * 9, 8 + x * 18, 84 + y * 18));//player inventory IDs 9 to 35
            }
        }

        for (int x = 0; x < 4; ++x)
        {
            final EntityEquipmentSlot entityequipmentslot = EQUIPMENT_SLOTS[x];
            this.addSlotToContainer(new Slot(armorInv, (3 - x), 8, 8 + x * 18)//Entity Equipment IDs 0 to 3
            {
                @Override
                public int getSlotStackLimit()
                {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack)
                {
                    return stack != null && stack.getItem().isValidArmor(stack, entityequipmentslot, null);
                }
                @Override
                @SideOnly(Side.CLIENT)
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        this.addSlotToContainer(new Slot(armorInv, 4, 28, 44));//Entity Equipment ID 4

        this.addSlotToContainer(new Slot(armorInv, 5, 28, 62){//Entity Equipment ID 5
            @Override
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        this.addSlotToContainer(new SlotAugment(armorInv, 6, 152, 5){
            @Override
            public int getSlotStackLimit()
            {
                return 1;
            }
        });//Entity Equipment ID 6

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(new Slot(entityInv, x + y * 3, 116 + x * 18, 25 + y * 18));//Entity Inventory 0 to 8
            }
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return entity.getOwner() != null && entity.getOwner().equals(playerIn);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
        Slot slot = getSlot(i);
        if (slot != null && slot.getHasStack()) {
            ItemStack is = slot.getStack();
            ItemStack result = is.copy();

            if (i >= 36) {
                if (!mergeItemStack(is, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (AugmentRegistry.getAugment(is) != null && !mergeItemStack(is, 36+6, 36 + entity.inventory.getSizeInventory() + entity.equipInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            } else if (!mergeItemStack(is, 36, 36 + entity.inventory.getSizeInventory() + entity.equipInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            if (is.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            slot.onTake(player, is);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onContainerClosed(EntityPlayer player){
        super.onContainerClosed(player);
        if(entity.world.isRemote)
            PacketDispatcher.sendToServer(new RequestAugmentMessage(entity));
    }
}
