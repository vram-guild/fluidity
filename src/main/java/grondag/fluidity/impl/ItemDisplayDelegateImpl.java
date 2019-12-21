package grondag.fluidity.impl;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;

import grondag.fluidity.api.client.ItemDisplayDelegate;

public class ItemDisplayDelegateImpl implements ItemDisplayDelegate {
	ItemStack stack;
	long count;
	int handle;
	String localizedName;
	String lowerCaseLocalizedName;

	public ItemDisplayDelegateImpl(ItemStack stack, long count, int handle) {
		set(stack, count, handle);
	}

	@Override
	public ItemDisplayDelegateImpl set (ItemStack stack, long count, int handle) {
		this.count = count;
		this.handle = handle;

		if(!(ItemStack.areItemsEqual(stack, this.stack) && ItemStack.areTagsEqual(stack, this.stack))) {
			this.stack = stack;
			localizedName = I18n.translate(stack.getTranslationKey());
			lowerCaseLocalizedName = localizedName.toLowerCase();
		}

		return this;
	}

	@Override
	public ItemDisplayDelegateImpl clone() {
		return new ItemDisplayDelegateImpl(stack, count, handle);
	}

	@Override
	public int handle() {
		return handle;
	}

	@Override
	public ItemStack displayStack() {
		return stack;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public String localizedName() {
		return localizedName;
	}

	@Override
	public String lowerCaseLocalizedName() {
		return lowerCaseLocalizedName;
	}
}
