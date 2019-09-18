package grondag.fluidity.api.item;

import grondag.fluidity.api.storage.DiscreteArticleView;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public interface StoredItemView extends DiscreteArticleView<StoredItemView> {
	Item item();
	
	CompoundTag tag();
	
	ItemStack toStack();
}
