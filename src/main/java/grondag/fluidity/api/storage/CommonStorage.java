package grondag.fluidity.api.storage;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.item.CommonItem;

public interface CommonStorage extends DiscreteStorage {
	default long accept(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return accept(CommonItem.of(item, tag), count, simulate);
	}

	default long accept(Item item, long count, boolean simulate) {
		return accept(CommonItem.of(item), count, simulate);
	}

	default long accept(ItemStack stack, long count, boolean simulate) {
		return accept(CommonItem.of(stack), count, simulate);
	}

	default long accept(ItemStack stack, boolean simulate) {
		return accept(CommonItem.of(stack), stack.getCount(), simulate);
	}

	default long supply(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return supply(CommonItem.of(item, tag), count, simulate);
	}

	default long supply(Item item, long count, boolean simulate) {
		return supply(CommonItem.of(item), count, simulate);
	}

	default long supply(ItemStack stack, long count, boolean simulate) {
		return supply(CommonItem.of(stack), count, simulate);
	}

	default long supply(ItemStack stack, boolean simulate) {
		return supply(CommonItem.of(stack), stack.getCount(), simulate);
	}
}
