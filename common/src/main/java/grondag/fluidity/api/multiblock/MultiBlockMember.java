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

package grondag.fluidity.api.multiblock;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Represents a type of "member" associate with a block, often related to a device component.
 * It is recommend that blocks and block entities <em>have</em> member instances instead of <em>being</em> members directly.
 * This will make it easier to exploit the prototype implementations in the {@code grondag.fluidity.base.multiblock} package,
 * and also make it possible to have a single block participate in more than one multiblock structure.
 *
 * @param <T> member type
 * @param <U> multi block type
 * @param <V> member component type (arbitrary)
 *
 * @see <a href="https://github.com/grondag/fluidity#multiblocks">https://github.com/grondag/fluidity#multiblocks</a>
 */
@Experimental
public interface MultiBlockMember<T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> {
	/**
	 * The multiblock to which this member belongs, or {@code null} if not currently part of one.
	 *
	 * @return the multiblock to which this member belongs
	 */
	@Nullable U getMultiblock();

	/**
	 * Called by the multiblock manager for this multiblock/member type when this member is added to
	 * or removed from a multiblock.
	 *
	 * @param owner The multiblock to which this member now belongs, or {@code null} if not part of one.
	 */
	void setMultiblock(@Nullable U owner);

	/**
	 * The author-determined device component or other instance this member contributes to its multiblock.
	 * The type and function of this attribute is entirely arbitrary.
	 *
	 * @return author-determined device component or other instance this member contributes to its multiblock
	 */
	V getMemberComponent();

	/**
	 * Position of this member in the world where it resides, packed as a long primitive using {@link BlockPos#asLong()}.
	 *
	 * <p>The Fluidity multiblock manager is optimized to use primitive values over BlockPos instances and will generally
	 * query this method instead of {@link #getBlockPos()}. Therefore implementations are advised to cache this vale
	 * or use it as the primary location attribute.
	 *
	 * @return osition of this member in the world where it resides, packed as a long primitive
	 */
	long getPackedPos();

	/**
	 * Position of this member in the world where it resides.
	 *
	 * <p>It is preferable to use {@link #getPackedPos()} when possible. See comments on that method.
	 *
	 * @return position of this member in the world where it resides
	 */
	default BlockPos getBlockPos() {
		return BlockPos.of(getPackedPos());
	}

	/**
	 * The world in which this member is located.
	 * Used with {@link #getPackedPos()} to unambiguously locate this member.
	 *
	 * @return the numeric ID of the world dimension for the world in which this member is located
	 */
	Level getWorld();
}
