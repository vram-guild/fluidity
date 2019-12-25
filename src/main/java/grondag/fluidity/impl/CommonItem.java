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
package grondag.fluidity.impl;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import grondag.fluidity.api.item.Article;
import grondag.fluidity.base.item.StackHelper;

@API(status = Status.EXPERIMENTAL)
public class CommonItem implements Article {
	protected Item item;
	protected CompoundTag tag;
	protected int hashCode;

	public CommonItem(Item item, @Nullable CompoundTag tag) {
		this.item = item;
		this.tag = tag;
		computeHash();
	}

	@Override
	public final boolean isEmpty() {
		return item == Items.AIR;
	}

	@Override
	public final Item getItem() {
		return item;
	}

	@Override
	public boolean hasTag() {
		return tag != null;
	}

	@Nullable
	public final CompoundTag copyTag() {
		return tag.copy();
	}

	@Override
	public final boolean doesTagMatch(@Nullable CompoundTag otherTag) {
		return tag == null ? otherTag == null : tag.equals(otherTag);
	}

	@Override
	public boolean isBulk() {
		return false;
	}

	@Override
	public Fluid toFluid() {
		return null;
	}

	@Override
	public boolean isCommon() {
		return true;
	}

	public CommonItem toImmutable() {
		return this;
	}

	@Override
	public final boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if(other instanceof CommonItem) {
			final CommonItem otherItem = (CommonItem) other;
			return otherItem.item == item && otherItem.tag == tag;
		} else {
			return false;
		}
	}

	protected void computeHash() {
		int hashCode = item.hashCode();

		if(tag != null) {
			hashCode += tag.hashCode();
		}

		this.hashCode = hashCode;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public final ItemStack toStack(long count) {
		final ItemStack result = new ItemStack(item, (int) Math.min(item.getMaxCount(), count));

		if (tag != null) {
			result.setTag(tag);
		}

		return result;
	}

	@Override
	public final ItemStack toStack() {
		return toStack(1);
	}

	@Override
	public boolean matches(ItemStack stack) {
		return StackHelper.areItemsEqual(item, tag, stack);
	}

	public static class Mutable extends CommonItem {
		public Mutable(Item item, CompoundTag tag) {
			super(item, tag);
		}

		public Mutable() {
			this(Items.AIR, null);
		}

		public final Mutable set(Item item) {
			return set(item, null);
		}

		public final Mutable set(Item item, CompoundTag tag) {
			this.item = item;
			this.tag = tag;
			computeHash();
			return this;
		}

		@Override
		public CommonItem toImmutable() {
			return new CommonItem(item, tag);
		}
	}

	public static final CommonItem NOTHING = new CommonItem(Items.AIR, null);

	private static final String TAG_KEY = "id"; // same as vanilla item stack

	@Override
	public void writeTag(CompoundTag tag, String tagName) {
		if(this.tag == null) {
			tag.putString(tagName, Registry.ITEM.getId(item).toString());
		} else {
			final CompoundTag myTag = this.tag.copy();
			myTag.putString(TAG_KEY, Registry.ITEM.getId(item).toString());
			tag.put(tagName, myTag);
		}
	}

	public static CommonItem fromTag(CompoundTag tag, String tagName) {
		final Tag data = tag.get(tagName);

		if(data == null) {
			return CommonItem.NOTHING;
		} else if(data.getType() == 8) {
			return of(Registry.ITEM.get(new Identifier(((StringTag) data).asString())), null);
		} else {
			final CompoundTag compound = (CompoundTag) data;
			final Item item =  Registry.ITEM.get(new Identifier(compound.getString(TAG_KEY)));
			compound.remove(TAG_KEY);
			return of(item, compound);
		}
	}

	// TODO: this needs to be a cache to prevent memory leaks in long game sessions when there are
	// many transient stack tag values going in and out of storage.  Implies all discrete comparisons must be equals.
	private static final ConcurrentHashMap<CommonItem, CommonItem> ITEMS = new ConcurrentHashMap<>();

	private static final ThreadLocal<Mutable> KEYS = ThreadLocal.withInitial(Mutable::new);

	public static CommonItem of(Item item) {
		return of(item, null);
	}

	public static CommonItem of(ItemStack stack) {
		return of(stack.getItem(), stack.getTag());
	}

	public static CommonItem of(Item item, @Nullable CompoundTag tag) {
		if(item == Items.AIR || item == null) {
			return CommonItem.NOTHING;
		} else {
			return ITEMS.computeIfAbsent(KEYS.get().set(item, tag), k -> k.toImmutable());
		}
	}
}
