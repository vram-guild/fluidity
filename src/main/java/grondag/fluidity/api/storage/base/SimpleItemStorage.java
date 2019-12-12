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
package grondag.fluidity.api.storage.base;

import java.util.Arrays;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.article.ItemStackView;
import grondag.fluidity.api.item.StackHelper;
import grondag.fluidity.api.storage.ItemStorage;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionHelper;

/**
 *
 * The naive, copy-all-the-stacks approach used here for transaction support is
 * heavy on allocation and could be problematic for very large inventories or very
 * large transaction. A journaling approach that captures changes as they are made
 * is likely to be preferable for performant implementations.  At a minimum, the
 * snapshot could be deferred until a change is made.
 *
 */
@API(status = Status.EXPERIMENTAL)
public class SimpleItemStorage extends AbstractStorage implements ItemStorage {
	protected int slotCount;
	protected ItemStack[] stacks;
	protected final ItemStackView view = new ItemStackView();

	public SimpleItemStorage(int slotCount) {
		this.slotCount = slotCount;
		stacks = new ItemStack[slotCount];
		Arrays.fill(stacks, ItemStack.EMPTY);
	}

	@Override
	public int slotCount() {
		return slotCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ArticleView> T view(int slot) {
		return( T) view.prepare(isSlotValid(slot) ? stacks[slot] : ItemStack.EMPTY, slot);
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		TransactionHelper.prepareInventoryRollbackHandler(context, this);
		return rollbackHandler;
	}

	@Override
	protected void handleRollback(TransactionContext context) {
		TransactionHelper.applyInventoryRollbackHandler(context, this);
	}

	@Override
	public ItemStack getInvStack(int slot) {
		return isSlotValid(slot) ? stacks[slot] : ItemStack.EMPTY;
	}

	@Override
	public void setInvStack(int slot, ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "ItemStack must be non-null");

		if (!isSlotValid(slot)) {
			return;
		}

		final ItemStack currentStack = stacks[slot];

		if (StackHelper.areStacksEqual(currentStack, itemStack)) {
			return;
		}

		stacks[slot] = itemStack;
		notifyListeners(slot);
		markDirty();
	}
}