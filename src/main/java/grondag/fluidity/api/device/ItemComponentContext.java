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
package grondag.fluidity.api.device;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sub-type of {@code ComponentContext} for item devices,
 * needed to carry item-specific information.
 */
@API(status = Status.EXPERIMENTAL)
public interface ItemComponentContext extends ComponentContext {
	/**
	 * The player holding the item in which the device component resides, or
	 * {@code null} if no player is present, as would be the case for automated
	 * access or in-world item entities.
	 *
	 * @return The player holding the item in which the device component resides
	 */
	@Nullable ServerPlayerEntity player();

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
