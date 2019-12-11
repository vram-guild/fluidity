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

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;

import grondag.fluidity.api.item.base.StackHelper;

@API(status = Status.EXPERIMENTAL)
public interface ItemStorage extends Storage, Inventory, RecipeInputProvider {
	/**
	 * Adds items to this storage. May return less than requested.
	 *
	 * @param item Item to add
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added, or that would be added if {@code simulate} = true.
	 */
	default long accept(Item item, CompoundTag tag, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item == Items.AIR) {
			return 0;
		}

		final int size = slotCount();
		long result = 0;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack candidate = getInvStack(i);

			if (candidate.isEmpty()) {
				final int n = (int) Math.min(count - result, item.getMaxCount());
				result += n;

				if (!simulate) {
					final ItemStack newStack = StackHelper.newStack(item, tag, n);
					// NB: notification handled in this call
					setInvStack(i, newStack);
					isDirty = true;
				}
			} else if (StackHelper.areItemsEqual(item, tag, candidate)) {
				final int capacity = candidate.getMaxCount() - candidate.getCount();

				if (capacity > 0) {
					final int n = (int) Math.min(count - result, capacity);
					result += n;

					if (!simulate) {
						candidate.increment(n);
						notifyListeners(i);
						isDirty = true;
					}
				}

			}
			if (result == count) {
				break;
			}
		}

		if (isDirty) {
			markDirty();
		}

		return result;
	}

	default long accept(Item item, long count, boolean simulate) {
		return accept(item, null, count, simulate);
	}

	default long accept(ItemStack stack, long count, boolean simulate) {
		return accept(stack.getItem(), stack.getTag(), count, simulate);
	}

	default long accept(ItemStack stack, boolean simulate) {
		return accept(stack.getItem(), stack.getTag(), stack.getCount(), simulate);
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
	default long supply(Item item, CompoundTag tag, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item == Items.AIR) {
			return 0;
		}

		final int size = slotCount();
		long result = 0;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack candidate = getInvStack(i);

			if (!candidate.isEmpty() && StackHelper.areItemsEqual(item, tag, candidate)) {
				final int n = (int) Math.min(count - result, candidate.getCount());

				if (n > 0) {
					result += n;

					if (!simulate) {
						candidate.decrement(n);
						notifyListeners(i);
						isDirty = true;
					}
				}

			}
			if (result == count) {
				break;
			}
		}

		if (isDirty) {
			markDirty();
		}

		return result;
	}

	default long supply(Item item, long count, boolean simulate) {
		return supply(item, null, count, simulate);
	}

	default long supply(ItemStack stack, long count, boolean simulate) {
		return supply(stack.getItem(), stack.getTag(), count, simulate);
	}

	default long supply(ItemStack stack, boolean simulate) {
		return supply(stack.getItem(), stack.getTag(), stack.getCount(), simulate);
	}

	@Override default boolean canPlayerUseInv(PlayerEntity player) {
		return true;
	}

	@Override
	default void provideRecipeInputs(RecipeFinder finder) {
		this.forEach(v -> {
			if (!v.isEmpty()  && v.isItem()) {
				finder.addItem(v.toItemView().toStack());
			}

			return true;
		});
	}

	@Override
	default int getInvSize() {
		return slotCount();
	}

	@Override
	default boolean isInvEmpty() {
		return isEmpty();
	}

	@Override
	default void markDirty() {
		//NOOP - default implementation doesn't support vanilla inventory listeners
	}

	@Override
	default void clear() {
		final int limit = slotCount();

		for (int i = 0; i < limit; i++) {
			final ItemStack stack = getInvStack(i);

			if (!stack.isEmpty()) {
				setInvStack(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	default ItemStack removeInvStack(int slot) {
		if (!isSlotValid(slot)) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = getInvStack(slot);

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		setInvStack(slot, ItemStack.EMPTY);

		return stack;
	}

	@Override
	default ItemStack takeInvStack(int slot, int count) {
		if (!isSlotValid(slot) || count == 0) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = getInvStack(slot);

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		final int n = Math.min(count, stack.getCount());
		final ItemStack result = stack.copy();
		result.setCount(n);
		stack.decrement(n);
		notifyListeners(slot);
		markDirty();

		return result;
	}
}
