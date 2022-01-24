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

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

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
	@Nullable ResourceLocation id();

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
	Level world();
}
