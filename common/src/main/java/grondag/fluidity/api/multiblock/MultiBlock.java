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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Represents a collection of member blocks in a world.
 * See sub-types in base package for prototype implementations.
 *
 * @param <T> member type
 * @param <U> multi block type
 * @param <V> member component type (arbitrary)
 *
 * @see <a href="https://github.com/grondag/fluidity#multiblocks">https://github.com/grondag/fluidity#multiblocks</a>
 */
@Experimental
public interface MultiBlock<T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> {
	/**
	 * Called when the multi block is destroyed because no members remain.
	 */
	default void close() { }

	/**
	 * Called when a member is added to this instance.
	 * The member will already be notified that it belongs to
	 * this instance before this method is called.
	 *
	 * @param member the member to be added to this instance
	 */
	void add(T member);

	/**
	 * Called when a member is removed from this instance.
	 * The member will be notified that it no longer belongs to
	 * this instance after this method returns.
	 *
	 * @param member the member being removed from this instance
	 */
	void remove(T member);

	/**
	 * Get the number of members currently in this instance.
	 *
	 * @return the number of members currently in this instance
	 */
	int memberCount();

	/**
	 * Applies the close action to all members, removes them from
	 * this instance and executes {@link #close()}.
	 *
	 * <p>Called when this instance is merged into another and this instance
	 * does not survive. May be called in other circumstances also.
	 *
	 * @param closeAction action to be applied to all members before removal
	 */
	void removalAllAndClose(Consumer<T> closeAction);
}
