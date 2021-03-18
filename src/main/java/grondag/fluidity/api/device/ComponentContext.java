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

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Data object that carries information to device components regarding
 * their state and the conditions in which they are being accessed.
 */
@Experimental
public interface ComponentContext {
	/**
	 * Identifies a requested location or sub-component within the device component,
	 * or {@code null} if the access request did not specify.
	 * Device component are not required to supports this feature.
	 *
	 * @return Identifier of a requested location or sub-component within the device component
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	@Nullable Identifier id();

	/**
	 * The side from which access to the device component was requested,
	 * or {@code null} if the access request did not specify a side.
	 *
	 * @return The side from which access to the device component was requested
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	@Nullable Direction side();

	/**
	 * The authorization token provided by the requester, or {@link Authorization#PUBLIC} if
	 * if the access request did not included an authorization token.
	 *
	 * @return The authorization token accompanying the request to access the device component
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	Authorization auth();

	/**
	 * The server-side game world in which the access request was made,
	 * and in which the device component is located.
	 *
	 * @return The server-side game world in which the device component is located
	 */
	World world();
}
