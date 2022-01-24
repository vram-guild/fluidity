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

package grondag.fluidity.api.device;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Sub-type of {@code ComponentContext} for item devices,
 * needed to carry item-specific information.
 */
@Experimental
public interface ItemComponentContext extends ComponentContext {
	/**
	 * The player holding the item in which the device component resides, or
	 * {@code null} if no player is present, as would be the case for automated
	 * access or in-world item entities.
	 *
	 * @return The player holding the item in which the device component resides
	 */
	@Nullable ServerPlayer player();

	/**
	 * Supplier for the item stack that houses the state for this device component.
	 * Return values should not be retained because there are no
	 * guarantees or restrictions that prevent it from being altered between calls.
	 *
	 * @return Supplier for the item stack that houses the state for this device component
	 */
	Supplier<ItemStack> stackGetter();

	/**
	 * Consumer used to persist the item stack that houses the state for this device component.
	 * The stack instance should not be retained because there are no
	 * guarantees or restrictions that prevent it from being altered between calls.
	 * Always use {@link #stackGetter()} to obtain a current instance.
	 *
	 * @return Consumer used to persist the item stack that houses the state for this device component
	 */
	Consumer<ItemStack> stackSetter();
}
