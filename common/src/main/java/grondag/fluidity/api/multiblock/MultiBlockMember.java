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
package grondag.fluidity.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

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
	 * Position of this member in the world where it resides, packed as a long primitive using {@link BlockPos#asLong()}.<p>
	 *
	 * The Fluidity multiblock manager is optimized to use primitive values over BlockPos instances and will generally
	 * query this method instead of {@link #getBlockPos()}. Therefore implementations are advised to cache this vale
	 * or use it as the primary location attribute.
	 *
	 * @return osition of this member in the world where it resides, packed as a long primitive
	 */
	long getPackedPos();

	/**
	 * Position of this member in the world where it resides.<p>
	 *
	 * It is preferable to use {@link #getPackedPos()} when possible. See comments on that method.
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
