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
package grondag.fluidity.api.storage;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.item.DiscreteItem;

@API(status = Status.EXPERIMENTAL)
public interface DiscreteStorage<A extends ItemArticleView> extends Storage<A, DiscreteStorageListener, DiscreteItem> {
	/**
	 * Adds items to this storage. May return less than requested.
	 *
	 * @param item Item to add
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added, or that would be added if {@code simulate} = true.
	 */
	long accept(DiscreteItem item, long count, boolean simulate);

	default long accept(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return accept(DiscreteItem.of(item, tag), count, simulate);
	}

	default long accept(Item item, long count, boolean simulate) {
		return accept(DiscreteItem.of(item), count, simulate);
	}

	default long accept(ItemStack stack, long count, boolean simulate) {
		return accept(DiscreteItem.of(stack), count, simulate);
	}

	default long accept(ItemStack stack, boolean simulate) {
		return accept(DiscreteItem.of(stack), stack.getCount(), simulate);
	}

	/**
	 * Removes items from this storage. May return less than requested.
	 *
	 * @param item Item to remove
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to remove. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count removed, or that would be removed if {@code simulate} = true.
	 */
	long supply(DiscreteItem item, long count, boolean simulate);

	default long supply(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return supply(DiscreteItem.of(item, tag), count, simulate);
	}

	default long supply(Item item, long count, boolean simulate) {
		return supply(DiscreteItem.of(item), count, simulate);
	}

	default long supply(ItemStack stack, long count, boolean simulate) {
		return supply(DiscreteItem.of(stack), count, simulate);
	}

	default long supply(ItemStack stack, boolean simulate) {
		return supply(DiscreteItem.of(stack), stack.getCount(), simulate);
	}
}
