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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sub-type of {@code ComponentContext} for block devices,
 * needed to carry block-specific information.
 */
@Experimental
public interface BlockComponentContext extends ComponentContext {
	/**
	 * Locates the block component within the world.
	 *
	 * @return Positing of the block component within the world.
	 */
	BlockPos pos();

	/**
	 * {@code BlockEntity} instance at {@link #pos()}, or {@code null} if there is none.
	 * This value is lazily retrieved and cached and in some cases may be pre-populated.
	 * Performance will be slightly improved using this method instead of retrieving the
	 * block entity from the world directly.
	 *
	 * @return {@code BlockEntity} instance at {@link #pos()}, or {@code null} if there is none
	 */
	@Nullable BlockEntity blockEntity();

	/**
	 * {@code Block} instance at {@link #pos()}. Will never be {@code null} but could be air.
	 *
	 * @return @code Block} instance at {@link #pos()}
	 */
	Block block();

	/**
	 * {@code BlockState} instance at {@link #pos()}. Will never be {@code null} but could be air.
	 *
	 * @return @code BlockState} instance at {@link #pos()}
	 */
	BlockState blockState();
}
