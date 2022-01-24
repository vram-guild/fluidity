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

package grondag.fluidity.api.storage;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;

import grondag.fluidity.base.storage.discrete.DiscreteStore;

/**
 * An extension of {@link Store} that implements {@link Container} with some default handlers.
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface InventoryStore extends DiscreteStore, Container, StackedContentsCompatible {
	@Override default boolean stillValid(Player player) {
		return true;
	}

	@Override
	default void fillStackedContents(StackedContents matcher) {
		this.forEach(v -> {
			if (!v.isEmpty()) {
				matcher.accountStack(v.toStack());
			}

			return true;
		});
	}

	@Override
	default boolean isEmpty() {
		return isEmpty();
	}
}
