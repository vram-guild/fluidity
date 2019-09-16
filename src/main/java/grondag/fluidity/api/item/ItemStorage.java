package grondag.fluidity.api.item;

import grondag.fluidity.api.discrete.DiscreteStorage;
import net.minecraft.item.ItemStack;

public interface ItemStorage extends DiscreteStorage<ItemPort, ItemStack, ItemArticleView> {
	@Override
	default ItemPort voidPort() {
		return ItemPort.VOID;
	}
	
	@Override
	default ItemArticleView emptyView() {
		return ItemArticleView.EMPTY;
	}
}
