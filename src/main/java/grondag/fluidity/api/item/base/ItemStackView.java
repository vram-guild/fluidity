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
package grondag.fluidity.api.item.base;

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.item.ItemArticleView;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class ItemStackView implements ItemArticleView, FractionView {
	protected ItemStack stack;
	protected int slot;

	public ItemStackView() {
	}

	public ItemStackView(ItemStack stack, int slot) {
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
	public ItemStack article() {
		return stack.copy();
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public FractionView volume() {
		return this;
	}

	@Override
	public long whole() {
		return count();
	}

	@Override
	public long numerator() {
		return 0;
	}

	@Override
	public long divisor() {
		return 1;
	}
}
