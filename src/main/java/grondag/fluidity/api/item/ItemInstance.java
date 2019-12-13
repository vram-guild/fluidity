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
package grondag.fluidity.api.item;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

@API(status = Status.EXPERIMENTAL)
public class ItemInstance {
	protected Item item;
	protected CompoundTag tag;

	public ItemInstance(Item item, @Nullable CompoundTag tag) {
		this.item = item;
		this.tag = tag;
	}

	public final boolean isEmpty() {
		return item == Items.AIR;
	}

	public final Item getItem() {
		return item;
	}

	@Nullable
	public final CompoundTag getTag() {
		return tag;
	}

	@Override
	public final boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if(other instanceof ItemInstance) {
			final ItemInstance otherItem = (ItemInstance) other;
			return otherItem.item == item && otherItem.tag == tag;
		} else {
			return false;
		}
	}

	public final ItemStack toStack(long count) {
		final ItemStack result = new ItemStack(item, (int) Math.min(item.getMaxCount(), count));

		if (tag != null) {
			result.setTag(tag);
		}

		return result;
	}

	public final ItemStack toStack() {
		return toStack(0);
	}

	public static ItemInstance of(Item item, @Nullable CompoundTag tag) {
		return new ItemInstance(item, tag);
	}

	public static ItemInstance of(Item item) {
		return new ItemInstance(item, null);
	}

	public static ItemInstance of(ItemStack stack) {
		return new ItemInstance(stack.getItem(), stack.getTag());
	}

	public static class MutableItemInstance extends ItemInstance {
		public MutableItemInstance(Item item, CompoundTag tag) {
			super(item, tag);
		}

		public MutableItemInstance() {
			this(Items.AIR, null);
		}

		public final void setItem(Item item) {
			this.item = item;
		}

		@Nullable
		public final void setTag(CompoundTag tag) {
			this.tag = tag ;
		}

		public final MutableItemInstance set(Item item, CompoundTag tag) {
			this.item = item;
			this.tag = tag;
			return this;
		}
	}

	public static final ItemInstance EMPTY = new ItemInstance(Items.AIR, null);
}
