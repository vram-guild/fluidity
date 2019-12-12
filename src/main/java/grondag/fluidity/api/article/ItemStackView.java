/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.api.article;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.item.StackHelper;

@API(status = Status.EXPERIMENTAL)
public class ItemStackView implements ItemArticleView {
	protected Item item;
	protected CompoundTag tag;
	protected long count;
	protected int slot;

	public ItemStackView() {
	}

	public ItemStackView(ItemStack stack, int slot) {
		prepare(stack, slot);
	}

	public ItemStackView prepare(ItemStack stack, int slot) {
		item = stack.getItem();
		count = stack.getCount();
		tag = stack.getTag();

		if (tag != null) {
			tag = tag.copy();
		}

		this.slot = slot;

		return this;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public int slot() {
		return slot;
	}

	@Override
	public Item item() {
		return item;
	}

	@Override
	public CompoundTag tag() {
		return tag == null ? null : (CompoundTag) tag.copy();
	}

	@Override
	public boolean hasTag() {
		return tag != null;
	}

	@Override
	public ItemStack toStack() {
		final ItemStack result = new ItemStack(item);
		result.setTag(tag.copy());
		return result;
	}

	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public boolean isBulk() {
		return false;
	}

	@Override
	public boolean isItemEqual(ItemStack stack) {
		return StackHelper.areItemsEqual(item, tag, stack);
	}

	public static ItemStackView of(ItemStack stack) {
		return new  ItemStackView().prepare(stack, 0);
	}
}
