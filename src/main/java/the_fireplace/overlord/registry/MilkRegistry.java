package the_fireplace.overlord.registry;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import the_fireplace.overlord.Overlord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author The_Fireplace
 */
public final class MilkRegistry {
    private static MilkRegistry instance;
    private HashMap<ItemStack, ItemStack> milks;

    private MilkRegistry(){
        milks = Maps.newHashMap();
    }

    public static MilkRegistry getInstance(){
        if(instance == null)
            instance = new MilkRegistry();
        return instance;
    }

    /**
     * Registers an item as Milk for the Skeleton Makers
     * @param inputItem
     *  The Milk itemstack
     * @param emptyItem
     *  The emptied version of the Milk itemstack.
     */
    public void registerMilk(@Nonnull ItemStack inputItem, ItemStack emptyItem){
        ItemStack inputCopy = new ItemStack(inputItem.getItem(), 1, inputItem.getMetadata());
        ItemStack emptyCopy = emptyItem != null ? new ItemStack(emptyItem.getItem(), 1, emptyItem.getMetadata()) : null;
        if(!milks.containsKey(inputCopy))
            milks.put(inputCopy, emptyCopy);
        else
            Overlord.logError("ItemStack was already registered as Milk: "+inputCopy.toString()+", skipping...");
    }

    /**
     * Checks if a stack is Milk
     * @param stackToCheck
     *  The stack to check
     * @return
     *  True if the stack is registered as Milk, false otherwise.
     */
    public boolean isMilk(ItemStack stackToCheck){
        for(ItemStack milk:milks.keySet()){
            if(milk.getItem() == stackToCheck.getItem() && (milk.getMetadata() == OreDictionary.WILDCARD_VALUE || milk.getMetadata() == stackToCheck.getMetadata()))
                return true;
        }
        return false;
    }

    /**
     * Returns an emptied version of the Milk ItemStack.
     * @param inputStack
     *  The Milk itemstack
     * @return
     *  The emptied ItemStack, or null if there isn't one.
     */
    @Nullable
    public ItemStack getEmptiedStack(ItemStack inputStack){
        if(inputStack == null)
            return null;
        for(ItemStack milk:milks.keySet()){
            if(milk.getItem() == inputStack.getItem() && (milk.getMetadata() == OreDictionary.WILDCARD_VALUE || milk.getMetadata() == inputStack.getMetadata()))
                return milks.get(milk);
        }
        return null;
    }
}
