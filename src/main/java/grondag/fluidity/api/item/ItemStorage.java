package grondag.fluidity.api.item;

import grondag.fluidity.api.storage.DiscreteStorage;
import net.minecraft.item.ItemStack;

public interface ItemStorage<T> extends DiscreteStorage<T, ItemStack, StoredItemView> {
	
	
}
