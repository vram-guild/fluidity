package grondag.fluidity.api.item;

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.WholeFraction;
import grondag.fluidity.api.storage.Storage;
import net.minecraft.item.ItemStack;

public interface ItemStorage<U> extends Storage<ItemStack, U, ItemArticleView> {
	public long capacity();

	public long capacityAvailable();
	
	default long capacityUsed() {
		return capacity() - capacityAvailable();
	}
	
	default FractionView accept(ItemStack article, FractionView volume, boolean simulate) {
		return WholeFraction.of(accept(article, volume.whole(), simulate));
	}

	default FractionView supply(ItemStack article, FractionView volume, boolean simulate) {
		return WholeFraction.of(supply(article, volume.whole(), simulate));
	}
}
