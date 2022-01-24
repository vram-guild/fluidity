/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.base.transact;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.storage.InventoryStore;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.impl.article.StackHelper;

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
