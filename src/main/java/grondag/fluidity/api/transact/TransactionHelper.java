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
package grondag.fluidity.api.transact;


import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import grondag.fluidity.api.item.StackHelper;
import grondag.fluidity.api.storage.ItemStorage;

@API(status = Status.EXPERIMENTAL)
public class TransactionHelper {
	public static void prepareInventoryRollbackHandler(TransactionContext context, ItemStorage storage) {
		final int size = storage.getInvSize();
		final ItemStack[] state = new ItemStack[size];

		for (int i = 0; i < size; i++) {
			state[i] = storage.getInvStack(i).copy();
		}

		context.setState(state);
	}

	public static void applyInventoryRollbackHandler(TransactionContext context, Inventory storage) {
		if (!context.isCommited()) {
			final int size = storage.getInvSize();
			final ItemStack[] state = context.getState();

			for (int i = 0; i < size; i++) {
				final ItemStack myStack = storage.getInvStack(i);
				final ItemStack stateStack = state[i];

				if (StackHelper.areStacksEqual(myStack, stateStack)) {
					continue;
				}

				storage.setInvStack(i, stateStack);
			}
		}
	}
}