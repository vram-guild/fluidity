/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.base.transact;


import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fluidity.api.storage.InventoryStore;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.impl.article.StackHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

@Experimental
public class TransactionHelper {

	public static Object prepareInventoryRollbackState(InventoryStore storage) {
		final int size = storage.getContainerSize();
		final ItemStack[] state = new ItemStack[size];

		for (int i = 0; i < size; i++) {
			state[i] = storage.getItem(i).copy();
		}

		return state;

	}

	public static void prepareInventoryRollbackHandler(TransactionContext context, InventoryStore storage) {
		context.setState(prepareInventoryRollbackState(storage));
	}

	public static void applyInventoryRollbackState(Object state, Container storage) {
		final int size = storage.getContainerSize();
		final ItemStack[] stacks = (ItemStack[]) state;

		for (int i = 0; i < size; i++) {
			final ItemStack myStack = storage.getItem(i);
			final ItemStack stateStack = stacks[i];

			if (StackHelper.areStacksEqual(myStack, stateStack)) {
				continue;
			}

			storage.setItem(i, stateStack);
		}
	}

	public static void applyInventoryRollbackHandler(TransactionContext context, Container storage) {
		if (!context.isCommited()) {
			applyInventoryRollbackState(context.getState(), storage);
		}
	}
}
