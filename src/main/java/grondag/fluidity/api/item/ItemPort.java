package grondag.fluidity.api.item;

import grondag.fluidity.api.discrete.DiscretePort;
import net.minecraft.item.ItemStack;

public interface ItemPort extends DiscretePort<ItemStack, ItemArticleView> {
	ItemPort VOID = new ItemPort() {
		@Override
		public long accept(ItemStack article, long count, int flags) {
			return 0;
		}

		@Override
		public long supply(ItemStack article, long count, int flags) {
			return 0;
		}
	};
}
