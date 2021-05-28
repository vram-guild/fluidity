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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;

import grondag.fluidity.base.storage.discrete.DiscreteStore;

/**
 * An extension of {@link Store} that implements {@link Inventory} with some default handlers.
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface InventoryStore extends DiscreteStore, Inventory, RecipeInputProvider {
	@Override default boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	default void provideRecipeInputs(RecipeMatcher matcher) {
		this.forEach(v -> {
			if (!v.isEmpty()) {
				matcher.addInput(v.toStack());
			}

			return true;
		});
	}

	@Override
	default boolean isEmpty() {
		return isEmpty();
	}
}
