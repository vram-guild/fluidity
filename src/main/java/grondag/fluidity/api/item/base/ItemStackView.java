package grondag.fluidity.api.item.base;

import grondag.fluidity.api.item.StoredItemView;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class ItemStackView implements StoredItemView {
	protected ItemStack stack;
	protected int slot;
	
	public ItemStackView() { }
	
	public ItemStackView (ItemStack stack, int slot) {
		prepare(stack, slot);
	}
	
	public ItemStackView prepare(ItemStack stack, int slot) {
		this.stack = stack;
		this.slot = slot;
		return this;
	}
	
	@Override
	public long count() {
		return stack.getCount();
	}

	@Override
	public int slot() {
		return slot;
	}

	@Override
	public Item item() {
		return stack.getItem();
	}

	@Override
	public CompoundTag tag() {
		return stack.getTag();
	}

	@Override
	public ItemStack toStack() {
		return stack.copy();
	}
}