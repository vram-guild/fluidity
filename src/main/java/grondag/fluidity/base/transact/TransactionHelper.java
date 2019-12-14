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
package grondag.fluidity.base.transact;


import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import grondag.fluidity.api.storage.InventoryStorage;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.base.item.StackHelper;

@API(status = Status.EXPERIMENTAL)
public class TransactionHelper {

	public static Object prepareInventoryRollbackState(InventoryStorage storage) {
		final int size = storage.getInvSize();
		final ItemStack[] state = new ItemStack[size];

		for (int i = 0; i < size; i++) {
			state[i] = storage.getInvStack(i).copy();
		}

		return state;

	}

	public static void prepareInventoryRollbackHandler(TransactionContext context, InventoryStorage storage) {
		context.setState(prepareInventoryRollbackState(storage));
	}

	public static void applyInventoryRollbackState(Object state, Inventory storage) {
		final int size = storage.getInvSize();
		final ItemStack[] stacks = (ItemStack[]) state;

		for (int i = 0; i < size; i++) {
			final ItemStack myStack = storage.getInvStack(i);
			final ItemStack stateStack = stacks[i];

			if (StackHelper.areStacksEqual(myStack, stateStack)) {
				continue;
			}

			storage.setInvStack(i, stateStack);
		}
	}

	public static void applyInventoryRollbackHandler(TransactionContext context, Inventory storage) {
		if (!context.isCommited()) {
			applyInventoryRollbackState(context.getState(), storage);
		}
	}
}
