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
package grondag.fluidity.api.storage;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;

import grondag.fluidity.base.storage.discrete.DiscreteStore;

/**
 *
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@API(status = Status.EXPERIMENTAL)
public interface InventoryStore extends DiscreteStore, Inventory, RecipeInputProvider {
	@Override default boolean canPlayerUseInv(PlayerEntity player) {
		return true;
	}

	@Override
	default void provideRecipeInputs(RecipeFinder finder) {
		this.forEach(v -> {
			if (!v.isEmpty()) {
				finder.addItem(v.toStack());
			}

			return true;
		});
	}

	@Override
	default boolean isInvEmpty() {
		return isEmpty();
	}

	@Override
	default void markDirty() {
		//NOOP - default implementation doesn't support vanilla inventory listeners
	}
}
