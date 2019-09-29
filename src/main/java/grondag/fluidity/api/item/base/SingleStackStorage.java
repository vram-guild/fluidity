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

import grondag.fluidity.api.storage.AbstractItemStorage;
import grondag.fluidity.api.storage.Storage;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;

public class SingleStackStorage extends AbstractItemStorage<Void> implements Storage<Void>, Inventory, RecipeInputProvider {
	protected ItemStack stack = ItemStack.EMPTY;

	@Override
	public int slotCount() {
		return 1;
	}

	@Override
	protected ItemStack getStack(int slot) {
		return slot == 0 ? stack : ItemStack.EMPTY;
	}

	@Override
	protected void setStack(int slot, ItemStack stack) {
		if (slot == 0) {
			this.stack = stack;
		}
	}
}
